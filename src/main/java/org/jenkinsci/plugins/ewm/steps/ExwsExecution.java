package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Fingerprint;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.WorkspaceList;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.definitions.Template;
import org.jenkinsci.plugins.ewm.model.ExternalWorkspace;
import org.jenkinsci.plugins.ewm.nodes.ExternalWorkspaceProperty;
import org.jenkinsci.plugins.ewm.nodes.NodeDisk;
import org.jenkinsci.plugins.ewm.nodes.NodeDiskPool;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.BodyExecution;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

/**
 * The execution of {@link ExwsStep}.
 *
 * @author Alexandru Somai
 */
public class ExwsExecution extends AbstractStepExecutionImpl {

    private static final long serialVersionUID = 1L;

    @Inject(optional = true)
    private transient ExwsStep step;

    @StepContextParameter
    private transient Computer computer;
    @StepContextParameter
    private transient TaskListener listener;
    @StepContextParameter
    private transient Run run;
    private BodyExecution body;

    @Override
    public boolean start() throws Exception {
        ExternalWorkspace exws = step.getExternalWorkspace();
        if (exws == null) {
            throw new AbortException("No external workspace provided. Did you run the exwsAllocate step?");
        }

        Node node = computer.getNode();
        if (node == null) {
            throw new Exception("The node is not live due to some unexpected conditions: the node might have been taken offline, or may have been removed");
        }

        String diskPoolId = exws.getDiskPoolId();
        NodeDiskPool nodeDiskPool;

        listener.getLogger().println("Searching for disk definitions in the External Workspace Templates from Jenkins global config");
        Template template = findTemplate(node.getLabelString(), step.getDescriptor().getTemplates());

        if (template != null) {
            nodeDiskPool = findNodeDiskPool(diskPoolId, template.getNodeDiskPools());
            if (nodeDiskPool == null) {
                String message = format("No Disk Pool Ref ID matching '%s' was found in the External Workspace Template config labeled '%s'", diskPoolId, template.getLabel());
                throw new AbortException(message);
            }
        } else {
            // fallback to finding the disk definitions into the node config
            listener.getLogger().println("Searching for disk definitions in the Node config");
            ExternalWorkspaceProperty exwsNodeProperty = findNodeProperty(node);

            nodeDiskPool = findNodeDiskPool(diskPoolId, exwsNodeProperty.getNodeDiskPools());
            if (nodeDiskPool == null) {
                String message = format("No Disk Pool Ref ID matching '%s' was found in Node '%s' config", diskPoolId, node.getDisplayName());
                throw new AbortException(message);
            }
        }

        NodeDisk nodeDisk = findNodeDisk(exws.getDiskId(), nodeDiskPool.getNodeDisks(), node.getDisplayName());

        FilePath diskFilePath = new FilePath(node.getChannel(), nodeDisk.getNodeMountPoint());
        FilePath baseWorkspace = diskFilePath.child(exws.getPathOnDisk());

        WorkspaceList.Lease lease = computer.getWorkspaceList().allocate(baseWorkspace);
        FilePath workspace = lease.path;

        updateFingerprint(exws.getId());

        listener.getLogger().println("Running in " + workspace);
        body = getContext().newBodyInvoker()
                .withContext(workspace)
                .withCallback(new Callback(getContext(), lease))
                .start();
        return false;
    }

    /**
     * Adds the current run to the fingerprint's usages.
     *
     * @param workspaceId the workspace's id
     * @throws IOException if fingerprint load operation fails,
     *                     or if no fingerprint is found for the given workspace id
     */
    private void updateFingerprint(String workspaceId) throws IOException {
        Fingerprint f = Jenkins.getActiveInstance()._getFingerprint(workspaceId);
        if (f == null) {
            throw new AbortException("Couldn't find any Fingerprint for: " + workspaceId);
        }

        Fingerprint.RangeSet set = f.getUsages().get(run.getParent().getFullName());
        if (set == null || !set.includes(run.getNumber())) {
            f.addFor(run);
            f.save();
        }
    }

    @Override
    public void stop(@Nonnull Throwable cause) throws Exception {
        if (body != null) {
            body.cancel(cause);
        }
    }

    /**
     * Finds a template from the given templates list that matches the given label String.
     *
     * @param nodeLabelString the label that the template has
     * @param templates       a list of templates
     * @return the template matching the given label, {@code null} otherwise
     */
    @CheckForNull
    private static Template findTemplate(String nodeLabelString, List<Template> templates) {
        Template selectedTemplate = null;
        for (Template template : templates) {
            String templateLabel = template.getLabel();
            if (templateLabel != null && nodeLabelString.contains(templateLabel)) {
                selectedTemplate = template;
                break;
            }
        }

        return selectedTemplate;
    }

    /**
     * Finds the {@link NodeProperty} for the external workspace definition.
     *
     * @param node the current node
     * @return the node property for the external workspace manager
     * @throws IOException if node property was not found
     */
    @Nonnull
    private static ExternalWorkspaceProperty findNodeProperty(Node node) throws IOException {
        DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties = node.getNodeProperties();

        ExternalWorkspaceProperty exwsNodeProperty = null;
        for (NodeProperty<?> nodeProperty : nodeProperties) {
            if (nodeProperty instanceof ExternalWorkspaceProperty) {
                exwsNodeProperty = (ExternalWorkspaceProperty) nodeProperty;
                break;
            }
        }

        if (exwsNodeProperty == null) {
            String message = format("There is no External Workspace config defined in Node '%s' config", node.getDisplayName());
            throw new AbortException(message);
        }

        return exwsNodeProperty;
    }

    /**
     * Selects the {@link NodeDiskPool} that has the {@link NodeDiskPool#diskPoolRefId} equal to the given
     * {@code diskPoolRefId} param.
     *
     * @param diskPoolRefId the disk pool reference id to be searching for
     * @param nodeDiskPools the list of disk pools
     * @return the Disk Pool that has the matching reference id, {@code null} otherwise
     */
    @CheckForNull
    private static NodeDiskPool findNodeDiskPool(@Nonnull String diskPoolRefId,
                                                 @Nonnull List<NodeDiskPool> nodeDiskPools) {
        NodeDiskPool selected = null;
        for (NodeDiskPool nodeDiskPool : nodeDiskPools) {
            if (diskPoolRefId.equals(nodeDiskPool.getDiskPoolRefId())) {
                selected = nodeDiskPool;
                break;
            }
        }

        return selected;
    }

    /**
     * Finds the disk definition from the given disks list that matches the given disk id.
     *
     * @param diskId    the disk id that the node definition should have
     * @param nodeDisks the list of available disk definitions
     * @param nodeName  the name of the current node
     * @return the disk definition that matches the given disk id
     * @throws IOException if no disk definition was found,
     *                     or if the disk definition has its node mount point null
     */
    @Nonnull
    private static NodeDisk findNodeDisk(String diskId, List<NodeDisk> nodeDisks, String nodeName) throws IOException {
        NodeDisk selected = null;
        for (NodeDisk nodeDisk : nodeDisks) {
            if (diskId.equals(nodeDisk.getDiskRefId())) {
                selected = nodeDisk;
                break;
            }
        }
        if (selected == null) {
            String message = format("The Node '%s' config does not have defined any Disk Ref ID '%s'", nodeName, diskId);
            throw new AbortException(message);
        }
        if (selected.getNodeMountPoint() == null) {
            String message = format("The Node '%s' config does not have defined any node mount point for Disk Ref ID '%s'", nodeName, diskId);
            throw new AbortException(message);
        }

        return selected;
    }

    private static final class Callback extends BodyExecutionCallback {

        private final StepContext context;
        private final WorkspaceList.Lease lease;

        Callback(StepContext context, WorkspaceList.Lease lease) {
            this.context = context;
            this.lease = lease;
        }

        @Override
        public void onSuccess(StepContext context, Object result) {
            lease.release();
            this.context.onSuccess(result);
        }

        @Override
        public void onFailure(StepContext context, Throwable t) {
            lease.release();
            this.context.onFailure(t);
        }
    }
}
