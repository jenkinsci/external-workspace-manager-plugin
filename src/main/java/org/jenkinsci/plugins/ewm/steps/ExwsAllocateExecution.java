package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.ewm.actions.ExwsAllocateAction;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.steps.model.ExternalWorkspace;
import org.jenkinsci.plugins.ewm.strategies.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.strategies.MostUsableSpaceStrategy;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import java.io.File;
import java.util.List;

import static hudson.Util.isRelativePath;
import static java.lang.String.format;

/**
 * The execution of the {@link ExwsAllocateStep}.
 *
 * @author Alexandru Somai
 */
public class ExwsAllocateExecution extends AbstractSynchronousNonBlockingStepExecution<ExternalWorkspace> {

    private static final long serialVersionUID = 1L;

    @Inject(optional = true)
    private transient ExwsAllocateStep step;

    @StepContextParameter
    private transient Run<?, ?> run;
    @StepContextParameter
    private transient TaskListener listener;
    @StepContextParameter
    private transient FlowNode flowNode;

    @Override
    protected ExternalWorkspace run() throws Exception {
        ExternalWorkspace exws;
        String upstreamName = step.getUpstream();
        if (upstreamName == null) {
            // this is the upstream job

            String diskPoolId = step.getDiskPoolId();
            if (diskPoolId == null) {
                throw new AbortException("Disk Pool ID was not provided as step parameter");
            }

            List<DiskPool> diskPools = step.getDescriptor().getDiskPools();
            DiskAllocationStrategy allocationStrategy = new MostUsableSpaceStrategy(diskPoolId, diskPools);
            Disk disk = allocationStrategy.allocateDisk();

            String physicalPathOnDisk = disk.getPhysicalPathOnDisk();
            if (physicalPathOnDisk == null) {
                String message = format("Physical path on disk was not provided in the Jenkins global config for the Disk Pool ID '%s'", diskPoolId);
                throw new AbortException(message);
            }
            String diskId = disk.getDiskId();
            if (diskId == null) {
                String message = format("Disk ID was not provided in the Jenkins global config for the Disk Pool ID '%s'", diskPoolId);
                throw new AbortException(message);
            }
            if (!isRelativePath(physicalPathOnDisk)) {
                String message = format("Physical path on disk defined for Disk ID '%s', within Disk Pool ID '%s' must be a relative path", diskId, diskPoolId);
                throw new AbortException(message);
            }

            String pathOnDisk = computePathOnDisk(physicalPathOnDisk);
            exws = new ExternalWorkspace(diskPoolId, diskId, pathOnDisk);
        } else {
            // this is the downstream job

            Item upstreamJob = findUpstreamJob(upstreamName);
            if (upstreamJob == null) {
                throw new AbortException(format("There isn't any upstream job associated with '%s'", upstreamName));
            }
            Run lastStableBuild = ((Job) upstreamJob).getLastStableBuild();
            if (lastStableBuild == null) {
                throw new AbortException(format("'%s' doesn't have any stable build", upstreamName));
            }
            if (!(lastStableBuild instanceof WorkflowRun)) {
                throw new AbortException(format("Build '%s' is not a Pipeline job. Can't read the run actions", lastStableBuild));
            }

            WorkflowRun lastStablePipeline = (WorkflowRun) lastStableBuild;
            ExwsAllocateAction exwsAllocateAction = findAction(lastStablePipeline.getExecution().getCurrentHeads());
            exws = exwsAllocateAction.getExternalWorkspace();
        }

        flowNode.addAction(new ExwsAllocateAction(flowNode, exws));

        listener.getLogger().println(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", exws.getDiskId(), exws.getDiskPoolId()));
        listener.getLogger().println(format("The path on Disk is: %s", exws.getPathOnDisk()));

        return exws;
    }

    /**
     * Computes the path to be used on the physical disk.
     * The computed path is like: physicalPathOnDisk/$JOB_NAME/$BUILD_NUMBER. Where $JOB_NAME also includes all the
     * folders, if Folders plugin is in use.
     *
     * @param physicalPathOnDisk the physical path on the disk
     * @return the computed file path on the physical disk
     */
    private String computePathOnDisk(String physicalPathOnDisk) {
        FilePath diskFilePath = new FilePath(new File(physicalPathOnDisk));
        return new FilePath(diskFilePath, run.getParent().getFullName() + "/" + run.getNumber()).getRemote();
    }

    /**
     * Find the upstream job that matches the given upstream name.
     *
     * @param upstreamJob the upstream job name
     * @return the upstream job with the given name if any, {@code null} otherwise
     */
    private Item findUpstreamJob(String upstreamJob) {
        for (Object itemObject : run.getParent().getParent().getItems()) {
            Item item = (Item) itemObject;
            if (upstreamJob.equals(item.getName())) {
                return item;
            }
        }

        return null;
    }

    /**
     * Iterates recursively, from bottom to top, through the list of registered flow nodes.
     * It searches for the action of type {@link ExwsAllocateAction} and returns it.
     *
     * @param flowNodes the flow nodes that may contain the needed action type
     * @return the Action registered at the exwsAllocate step, or {@code null} if not found
     */
    private static ExwsAllocateAction findAction(List<FlowNode> flowNodes) {
        ExwsAllocateAction action = null;
        for (FlowNode node : flowNodes) {
            action = node.getAction(ExwsAllocateAction.class);
            if (action == null) {
                action = findAction(node.getParents());
            }
        }

        return action;
    }
}
