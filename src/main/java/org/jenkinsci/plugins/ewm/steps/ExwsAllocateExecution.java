package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.actions.ExternalWorkspaceActionImpl;
import org.jenkinsci.plugins.ewm.actions.ExwsAllocateActionImpl;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.model.ExternalWorkspace;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static hudson.Util.isRelativePath;
import static hudson.Util.replaceMacro;
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
    private transient EnvVars envVars;
    @StepContextParameter
    private transient FlowNode flowNode;

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
            DiskPool diskPool = findDiskPool(diskPoolId, diskPools);

            DiskAllocationStrategy strategy = step.getStrategy();
            if (strategy == null) {
                listener.getLogger().println("Disk allocation strategy was not provided as step parameter. " +
                        "Fallback to the strategy defined in the Jenkins global config");
                strategy = diskPool.getStrategy();
            }

            listener.getLogger().println(format("Using Disk allocation strategy: '%s'", strategy.getDescriptor().getDisplayName()));
            Disk disk = strategy.allocateDisk(diskPool.getDisks());

            String diskId = disk.getDiskId();
            if (diskId == null) {
                String message = format("Disk ID was not provided in the Jenkins global config for the Disk Pool ID '%s'", diskPoolId);
                throw new AbortException(message);
            }

            String pathOnDisk;
            String customPath = step.getPath();
            if (customPath != null) {
                pathOnDisk = computeCustomPath(customPath);

            } else {
                String workspaceTemplate = diskPool.getWorkspaceTemplate();
                if (workspaceTemplate != null) {
                    pathOnDisk = computePathBasedOnTemplate(workspaceTemplate);

                } else {
                    pathOnDisk = computeDefaultPathOnDisk(diskId, disk.getPhysicalPathOnDisk());
                }
            }

            String masterMountPoint = disk.getMasterMountPoint();
            if (masterMountPoint == null) {
                String message = String.format("Mounting point from Master to the disk is not defined for Disk ID '%s'", diskId);
                throw new AbortException(message);
            }
            exws = new ExternalWorkspace(diskPoolId, diskId, masterMountPoint, pathOnDisk);
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

        String diskPoolId = exws.getDiskPoolId();
        DiskPool diskPool = findDiskPool(diskPoolId, step.getDescriptor().getDiskPools());

        JobRestriction restriction = diskPool.getRestriction();
        if (!diskPool.getRestriction().canTake(run)) {
            String message = format("Disk Pool identified by '%s' is not accessible due to the applied Disk Pool restriction: %s", diskPoolId, restriction.getDescriptor().getDisplayName());
            throw new AbortException(message);
        }

        ExternalWorkspaceActionImpl externalWorkspaceAction = new ExternalWorkspaceActionImpl(exws, flowNode);
        flowNode.addAction(externalWorkspaceAction);
        exws.setWorkspaceUrl(flowNode.getUrl() + externalWorkspaceAction.getUrlName());

        ExwsAllocateActionImpl allocateAction = run.getAction(ExwsAllocateActionImpl.class);
        if (allocateAction == null) {
            allocateAction = new ExwsAllocateActionImpl();
            run.addAction(allocateAction);
        }
        allocateAction.addAllocatedWorkspace(exws);
        run.save();

        listener.getLogger().println(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", exws.getDiskId(), diskPoolId));
        listener.getLogger().println(format("The path on Disk is: %s", exws.getPathOnDisk()));

        return exws;
    }

    /**
     * Iterates through given disk pool list and finds the {@link DiskPool} that has the {@link DiskPool#diskPoolId}
     * equal to the given disk pool id parameter.
     *
     * @param diskPoolId the matching id that the disk pool should have
     * @param diskPools  the list of disk pools
     * @return the disk pool whose {@link DiskPool#diskPoolId} is equal to the given disk pool id parameter
     * @throws IOException if there isn't find any disk pool matching the disk pool id, or
     *                     if the disk pool doesn't have defined any {@link Disk} entries
     */
    @Nonnull
    private static DiskPool findDiskPool(@Nonnull String diskPoolId, @Nonnull List<DiskPool> diskPools) throws IOException {
        DiskPool diskPool = null;
        for (DiskPool dp : diskPools) {
            if (diskPoolId.equals(dp.getDiskPoolId())) {
                diskPool = dp;
                break;
            }
        }

        if (diskPool == null) {
            String message = format("No Disk Pool ID matching '%s' was found in the global config", diskPoolId);
            throw new AbortException(message);
        }

        if (diskPool.getDisks().isEmpty()) {
            String message = String.format("No Disks were defined in the global config for Disk Pool ID '%s'", diskPoolId);
            throw new AbortException(message);
        }

        return diskPool;
    }

    /**
     * Normalizes the given custom path and returns it.
     *
     * @param customPath the workspace path to be used on the disk
     * @return the normalized custom path
     * @throws IOException if the {@code customPath} is not a relative path, or if it contains any '$' characters,
     *                     meaning that the path was not resolved correctly in the Pipeline script
     */
    @Nonnull
    private String computeCustomPath(@Nonnull String customPath) throws IOException {
        if (!isRelativePath(customPath)) {
            String message = format("The custom path: %s must be a relative path", customPath);
            throw new AbortException(message);
        }
        if (customPath.contains("${")) {
            String message = format("The custom path: %s contains '${' characters. Did you resolve correctly the parameters with Build DSL?", customPath);
            throw new AbortException(message);
        }

        return new FilePath(new File(customPath)).getRemote();
    }

    /**
     * Computes the path to be used on the physical disk.
     * The computed path has the following pattern: physicalPathOnDisk/$JOB_NAME/$BUILD_NUMBER.
     * Where $JOB_NAME also includes all the folders, if Folders plugin is in use.
     *
     * @param diskId             the Disk ID where the physical path on the disk is defined
     * @param physicalPathOnDisk the physical path on the disk
     * @return the computed file path on the physical disk
     * @throws IOException if the {@code physicalPathOnDisk} argument is not a relative path
     */
    @Nonnull
    private String computeDefaultPathOnDisk(@Nonnull String diskId, @CheckForNull String physicalPathOnDisk) throws IOException {
        if (physicalPathOnDisk == null) {
            physicalPathOnDisk = StringUtils.EMPTY;
        }
        if (!isRelativePath(physicalPathOnDisk)) {
            String message = format("Physical path on disk defined for Disk ID '%s', within Disk Pool ID '%s' must be a relative path", diskId, step.getDiskPoolId());
            throw new AbortException(message);
        }

        File pathOnDisk = Paths.get(physicalPathOnDisk, run.getParent().getFullName(), String.valueOf(run.getNumber())).toFile();
        return new FilePath(pathOnDisk).getRemote();
    }

    /**
     * Computes the workspace path based on the given template.
     * It replaces the occurrences of $PARAM with their corresponding values from the environment variables.
     *
     * @param template the template to compute the workspace path based on
     * @return the computed workspace path
     * @throws IOException if the {@code template} argument is not a relative path or if it can't be resolved correctly
     */
    @Nonnull
    private String computePathBasedOnTemplate(@Nonnull String template) throws IOException {
        if (!isRelativePath(template)) {
            throw new AbortException(format("Workspace template defined for Disk Pool '%s' must be a relative path", step.getDiskPoolId()));
        }

        String path = replaceMacro(template, envVars);
        if (path == null) {
            // The resulting String from Util#replaceMacro is null only if the input String is null.
            // In this case the input String is not null, so this exception may never occur.
            String message = format("Path is null after resolving environment variables for the defined workspace template: %s", template);
            throw new AbortException(message);
        }
        if (path.contains("${")) {
            // If the workspace template is resolved correctly, the resulting path shouldn't contain any '$' characters.
            String message = format("Can't resolve the following workspace template: %s. The resulting path is: %s. " +
                    "Did you provide all the needed environment variables?", template, path);
            throw new AbortException(message);
        }

        return new FilePath(new File(path)).getRemote();
    }
}
