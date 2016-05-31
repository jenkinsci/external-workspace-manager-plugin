package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.steps.model.ExternalWorkspace;
import org.jenkinsci.plugins.ewm.strategies.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.strategies.MostUsableSpaceStrategy;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import java.io.File;
import java.util.List;

/**
 * The execution of the {@link ExwsAllocateStep}.
 *
 * @author Alexandru Somai
 */
public class ExwsAllocateExecution extends AbstractSynchronousNonBlockingStepExecution<ExternalWorkspace> {

    @Inject(optional = true)
    private transient ExwsAllocateStep step;

    @StepContextParameter
    private transient Run<?, ?> run;
    @StepContextParameter
    private transient TaskListener listener;

    @Override
    protected ExternalWorkspace run() throws Exception {
        if (step.getUpstream() == null) {
            // this is the upstream job

            String diskPoolId = step.getDiskPoolId();
            if (diskPoolId == null) {
                throw new Exception("Disk Pool ID was not provided as step parameter");
            }

            List<DiskPool> diskPools = step.getDescriptor().getDiskPools();
            DiskAllocationStrategy allocationStrategy = new MostUsableSpaceStrategy(diskPoolId, diskPools);
            Disk disk = allocationStrategy.allocateDisk();

            String physicalPathOnDisk = disk.getPhysicalPathOnDisk();
            if (physicalPathOnDisk == null) {
                throw new Exception("Physical path on disk was not provided in the Jenkins global config");
            }
            String diskId = disk.getDiskId();
            if (diskId == null) {
                throw new Exception("Disk ID was not provided in the Jenkins global config");
            }

            String pathOnDisk = computePathOnDisk(physicalPathOnDisk);
            ExternalWorkspace externalWorkspace = new ExternalWorkspace(diskId, pathOnDisk);

            listener.getLogger().println(String.format("Selected disk id is: %s", externalWorkspace.getDiskId()));
            listener.getLogger().println(String.format("The path on disk is: %s", externalWorkspace.getPathOnDisk()));

            return externalWorkspace;
        } else {
            // this is the downstream job
            // TODO implement
            return null;
        }
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
