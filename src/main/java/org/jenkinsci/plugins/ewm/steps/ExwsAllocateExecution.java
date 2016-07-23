package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.ewm.DiskPoolRestriction;
import org.jenkinsci.plugins.ewm.actions.ExwsAllocateActionImpl;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.restrictions.NoDiskPoolRestriction;
import org.jenkinsci.plugins.ewm.steps.model.ExternalWorkspace;
import org.jenkinsci.plugins.ewm.strategies.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.strategies.MostUsableSpaceStrategy;
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

    private static final DiskPoolRestriction NO_DISK_POOL_RESTRICTION = new NoDiskPoolRestriction();

    @Inject(optional = true)
    private transient ExwsAllocateStep step;

    @StepContextParameter
    private transient Run<?, ?> run;
    @StepContextParameter
    private transient TaskListener listener;

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

            // TODO - when #24 is merged, the following iteration will not be required! This is temporary
            DiskPool diskPool = new DiskPool(null, null, null, null);
            for (DiskPool dp : diskPools) {
                if (diskPoolId.equals(dp.getDiskPoolId())) {
                    diskPool = dp;
                }
            }

            DiskPoolRestriction restriction = diskPool.getRestriction();
            if (restriction == null) {
                restriction = NO_DISK_POOL_RESTRICTION;
            }
            if (!restriction.isAllowed(run, listener)) {
                String message = format("Disk Pool ID: '%s' is not accessible due to the applied Disk Pool restriction: %s", diskPoolId, restriction);
                throw new AbortException(message);
            }

            String diskId = disk.getDiskId();
            if (diskId == null) {
                String message = format("Disk ID was not provided in the Jenkins global config for the Disk Pool ID '%s'", diskPoolId);
                throw new AbortException(message);
            }
            String physicalPathOnDisk = disk.getPhysicalPathOnDisk();
            if (physicalPathOnDisk == null) {
                physicalPathOnDisk = StringUtils.EMPTY;
            }
            if (!isRelativePath(physicalPathOnDisk)) {
                String message = format("Physical path on disk defined for Disk ID '%s', within Disk Pool ID '%s' must be a relative path", diskId, diskPoolId);
                throw new AbortException(message);
            }

            String pathOnDisk = computePathOnDisk(physicalPathOnDisk);
            exws = new ExternalWorkspace(diskPoolId, diskId, pathOnDisk);
        } else {
            // this is the downstream job

            if (step.getDiskPoolId() != null) {
                listener.getLogger().println("WARNING: Both 'upstream' and 'diskPoolId' parameters were provided. " +
                        "The 'diskPoolId' parameter will be ignored. The step will allocate the workspace used by the upstream job.");
            }

            Item upstreamJob = Jenkins.getActiveInstance().getItemByFullName(upstreamName);
            if (upstreamJob == null) {
                throw new AbortException(format("Can't find any upstream Jenkins job by the full name '%s'. Are you sure that this is the full project name?", upstreamName));
            }
            Run lastStableBuild = ((Job) upstreamJob).getLastStableBuild();
            if (lastStableBuild == null) {
                throw new AbortException(format("'%s' doesn't have any stable build", upstreamName));
            }

            ExwsAllocateActionImpl allocateAction = lastStableBuild.getAction(ExwsAllocateActionImpl.class);
            if (allocateAction == null) {
                String message = format("The upstream job '%s' must have at least one stable build with a call to the " +
                        "exwsAllocate step in order to have a workspace usable by this job.", upstreamName);
                throw new AbortException(message);
            }

            List<ExternalWorkspace> allocatedWorkspaces = allocateAction.getAllocatedWorkspaces();
            if (allocatedWorkspaces.size() > 1) {
                listener.getLogger().println(format("WARNING: The Jenkins job '%s' have recorded multiple external workspace allocations. " +
                        "Did you call exwsAllocate step multiple times in the same run? This downstream Jenkins job will use the first recorded workspace allocation.", upstreamName));
            }

            // this list always contains at least one element
            exws = allocatedWorkspaces.iterator().next();
        }

        ExwsAllocateActionImpl allocateAction = run.getAction(ExwsAllocateActionImpl.class);
        if (allocateAction == null) {
            allocateAction = new ExwsAllocateActionImpl();
            run.addAction(allocateAction);
        }
        allocateAction.addAllocatedWorkspace(exws);
        run.save();

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
}
