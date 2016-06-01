package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.WorkspaceList;
import hudson.util.DescribableList;
import org.jenkinsci.plugins.ewm.definitions.Template;
import org.jenkinsci.plugins.ewm.nodes.DiskNode;
import org.jenkinsci.plugins.ewm.nodes.ExternalWorkspaceProperty;
import org.jenkinsci.plugins.ewm.steps.model.ExternalWorkspace;
import org.jenkinsci.plugins.workflow.steps.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

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
    private BodyExecution body;

    @Override
    public boolean start() throws Exception {
        ExternalWorkspace exws = step.getExternalWorkspace();
        if (exws == null) {
            throw new AbortException("No external workspace provided. Did you run the exwsAllocate step?");
        }

        Node node = computer.getNode();
        if (node == null) {
            throw new Exception("Computer does not correspond to a live node");
        }

        Template template = findTemplate(exws.getDiskPoolId(), node.getLabelString(), step.getDescriptor().getTemplates());
        List<DiskNode> diskNodes;
        if (template != null) {
            diskNodes = template.getDiskNodes();
        } else {
            // fallback to finding the disk definitions into the node config
            ExternalWorkspaceProperty exwsNodeProperty = findNodeProperty(exws.getDiskPoolId(), node.getNodeProperties());
            diskNodes = exwsNodeProperty.getDiskNodes();
        }

        DiskNode diskNode = findDiskNode(exws.getDiskId(), diskNodes);

        FilePath diskFilePath = new FilePath(node.getChannel(), diskNode.getLocalRootPath());
        FilePath baseWorkspace = diskFilePath.child(exws.getPathOnDisk());

        WorkspaceList.Lease lease = computer.getWorkspaceList().allocate(baseWorkspace);
        FilePath workspace = lease.path;
        listener.getLogger().println("Running in " + workspace);
        body = getContext().newBodyInvoker()
                .withContext(workspace)
                .withCallback(new Callback(getContext(), lease))
                .withDisplayName(null)
                .start();
        return false;
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
     * @param diskPoolRefId   the disk pool ref id that the found template should have
     * @param nodeLabelString the label that the template has
     * @param templates       a list of templates
     * @return the template matching the given label, <code>null</code> otherwise
     * @throws IOException if the found template doesn't have defined a disk pool ref id
     *                     if the defined disk pool ref id doesn't match the one given as parameter
     */
    @CheckForNull
    private static Template findTemplate(String diskPoolRefId, String nodeLabelString, List<Template> templates) throws IOException {
        Template selectedTemplate = null;
        for (Template template : templates) {
            String templateLabel = template.getLabel();
            if (templateLabel != null && nodeLabelString.contains(templateLabel)) {
                selectedTemplate = template;
                break;
            }
        }
        if (selectedTemplate != null) {
            String templateDiskPoolRefId = selectedTemplate.getDiskPoolRefId();
            if (templateDiskPoolRefId == null) {
                throw new AbortException("The Disk Pool Ref ID was not provided in Jenkins global config");
            }
            if (!templateDiskPoolRefId.equals(diskPoolRefId)) {
                throw new AbortException("The Disk Pool Ref ID defined in Jenkins global config does not match the one allocated by the exwsAllocate step");
            }
        }

        return selectedTemplate;
    }

    /**
     * Finds the {@link NodeProperty} for the external workspace definition.
     *
     * @param diskPoolRefId  the disk pool ref id that the node property should have
     * @param nodeProperties all the the available node properties
     * @return the node property for the external workspace manager
     * @throws IOException if no node property was found
     *                     if the node property doesn't have defined any disk pool ref id
     *                     if the disk pool ref id doesn't match the given disk pool ref id
     */
    @Nonnull
    private static ExternalWorkspaceProperty findNodeProperty(String diskPoolRefId,
                                                              DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties) throws IOException {
        ExternalWorkspaceProperty exwsNodeProperty = null;
        for (NodeProperty<?> nodeProperty : nodeProperties) {
            if (nodeProperty instanceof ExternalWorkspaceProperty) {
                exwsNodeProperty = (ExternalWorkspaceProperty) nodeProperty;
                break;
            }
        }
        if (exwsNodeProperty == null) {
            throw new AbortException("There is no External Workspace config defined in Node config");
        }
        String nodeDiskPoolRefId = exwsNodeProperty.getDiskPoolRefId();
        if (nodeDiskPoolRefId == null) {
            throw new AbortException("The Disk Pool Ref ID was not provided in the node config");
        }
        if (!nodeDiskPoolRefId.equals(diskPoolRefId)) {
            throw new AbortException("The Disk Pool Ref ID defined in the node config does not match the one allocate by the exwsAllocate step");
        }

        return exwsNodeProperty;
    }

    /**
     * Finds the disk definition from the given disks list that matches the given disk id.
     *
     * @param diskId    the disk id that the node definition should have
     * @param diskNodes the list of available disk definitions
     * @return the disk definition that matches the given disk id
     * @throws IOException if no disk definition was found
     *                     if the disk definition has its local root path null
     */
    @Nonnull
    private static DiskNode findDiskNode(String diskId, List<DiskNode> diskNodes) throws IOException {
        DiskNode selectedDiskNode = null;
        for (DiskNode diskNode : diskNodes) {
            if (diskId.equals(diskNode.getDiskRefId())) {
                selectedDiskNode = diskNode;
                break;
            }
        }
        if (selectedDiskNode == null) {
            throw new AbortException(String.format("Couldn't find any node configuration matching '%s'", diskId));
        }
        if (selectedDiskNode.getLocalRootPath() == null) {
            throw new AbortException(String.format("The local root path should not be null for disk '%s'", diskId));
        }

        return selectedDiskNode;
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
