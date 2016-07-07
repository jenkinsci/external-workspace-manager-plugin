package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.actions.ExwsAllocateActionImpl;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.steps.model.ExternalWorkspace;
import org.jenkinsci.plugins.ewm.strategies.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.strategies.MostUsableSpaceStrategy;
import org.jenkinsci.plugins.runselector.context.RunSelectorPickContext;
import org.jenkinsci.plugins.runselector.selectors.RunSelector;
import org.jenkinsci.plugins.runselector.selectors.StatusRunSelector;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import java.io.File;
import java.io.IOException;
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
    private static final RunSelector DEFAULT_RUN_SELECTOR = new StatusRunSelector(StatusRunSelector.BuildStatus.Stable);

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

            String physicalPathOnDisk = disk.getPhysicalPathOnDisk();
            String diskId = disk.getDiskId();
            if (diskId == null) {
                String message = format("Disk ID was not provided in the Jenkins global config for the Disk Pool ID '%s'", diskPoolId);
                throw new AbortException(message);
            }
            if (physicalPathOnDisk == null) {
                String message = format("Physical path on disk was not provided in the Jenkins global config for Disk ID: '%s', within Disk Pool ID '%s'", diskId, diskPoolId);
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

            RunSelector selector = step.getSelector();
            if (selector == null) {
                listener.getLogger().println(format("No selector provided. Using the default build selector: %s", DEFAULT_RUN_SELECTOR.getDescriptor().getDisplayName()));
                selector = DEFAULT_RUN_SELECTOR;
            }

            if (step.getDiskPoolId() != null) {
                listener.getLogger().println("WARNING: Both 'upstream' and 'diskPoolId' parameters were provided. " +
                        "The 'diskPoolId' parameter will be ignored. The step will allocate the workspace used by the upstream job.");
            }

            Job<?, ?> upstreamJob = Jenkins.getActiveInstance().getItemByFullName(upstreamName, Job.class);
            if (upstreamJob == null) {
                throw new AbortException(format("Can't find any upstream Jenkins job by the full name '%s'. Are you sure that this is the full project name?", upstreamName));
            }

            EnvVars envVars = getEnvVars();
            RunSelectorPickContext context = new RunSelectorPickContext();
            context.setJenkins(Jenkins.getInstance());
            context.setCopierBuild(run);
            context.setListener(listener);
            context.setEnvVars(envVars);
            context.setVerbose(step.isVerbose());

            String jobName = envVars.expand(upstreamName);
            context.setProjectName(jobName);
            context.setRunFilter(step.getRunFilter());

            Run<?, ?> upstreamBuild = selector.pickBuildToCopyFrom(upstreamJob, context);

            if (upstreamBuild == null) {
                throw new AbortException(format("Unable to find a build within upstream job '%s'", upstreamName));
            }

            ExwsAllocateActionImpl allocateAction = upstreamBuild.getAction(ExwsAllocateActionImpl.class);
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
     * Gets the environment variables based on the current run.
     *
     * @return the {@link EnvVars} for the current run
     * @throws IOException
     * @throws InterruptedException
     */
    private EnvVars getEnvVars() throws IOException, InterruptedException {
        EnvVars envVars = run.getEnvironment(listener);
        if (run instanceof AbstractBuild) {
            envVars.overrideAll(((AbstractBuild<?, ?>) run).getBuildVariables());
        } else {
            for (ParametersAction pa : run.getActions(ParametersAction.class)) {
                for (ParameterValue pv : pa.getParameters()) {
                    pv.buildEnvironment(run, envVars);
                }
            }
        }

        return envVars;
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
