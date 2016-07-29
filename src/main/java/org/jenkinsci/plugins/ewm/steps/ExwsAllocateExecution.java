package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.ewm.actions.ExwsAllocateActionImpl;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.steps.model.ExternalWorkspace;
import org.jenkinsci.plugins.ewm.strategies.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.strategies.MostUsableSpaceStrategy;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;

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

    @Override
    protected ExternalWorkspace run() throws Exception {
        ExternalWorkspace exws;
        RunWrapper selectedRunWrapper = step.getSelectedRun();
        if (selectedRunWrapper == null) {
            // this is the upstream job

            String diskPoolId = step.getDiskPoolId();
            if (diskPoolId == null) {
                throw new AbortException("Disk Pool ID was not provided as step parameter");
            }

            List<DiskPool> diskPools = step.getDescriptor().getDiskPools();
            DiskAllocationStrategy allocationStrategy = new MostUsableSpaceStrategy(diskPoolId, diskPools);
            Disk disk = allocationStrategy.allocateDisk();

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
                listener.getLogger().println("WARNING: Both 'selectedRun' and 'diskPoolId' parameters were provided. " +
                        "The 'diskPoolId' parameter will be ignored. The step will allocate the workspace used by the selected run.");
            }

            Run<?, ?> selectedRun = selectedRunWrapper.getRawBuild();
            if (selectedRun == null) {
                throw new AbortException("The selected RunWrapper object contains a null Run. Possibly this run has been deleted in the meantime?");
            }
            ExwsAllocateActionImpl allocateAction = selectedRun.getAction(ExwsAllocateActionImpl.class);
            if (allocateAction == null) {
                String message = format("The selected run '%s' must have at least one call to the " +
                        "exwsAllocate step in order to have a workspace usable by this job.", selectedRun);
                throw new AbortException(message);
            }

            List<ExternalWorkspace> allocatedWorkspaces = allocateAction.getAllocatedWorkspaces();
            if (allocatedWorkspaces.size() > 1) {
                listener.getLogger().println(format("WARNING: The selected run '%s' have recorded multiple external workspace allocations. " +
                        "Did you call exwsAllocate step multiple times in the same run? This downstream Jenkins job will use the first recorded workspace allocation.", selectedRun));
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
