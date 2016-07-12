package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.model.Job;
import hudson.model.PermalinkProjectAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;

import static java.lang.String.format;

/**
 * @author Alexandru Somai
 */
public class RunSelectorExecution extends AbstractSynchronousNonBlockingStepExecution<RunWrapper> {

    @Inject
    private transient RunSelectorStep step;

    @StepContextParameter
    private transient Run<?, ?> run;
    @StepContextParameter
    private transient TaskListener listener;

    @Override
    protected RunWrapper run() throws Exception {

        // TODO more detailed error messages

        String jobName = step.getJobName();
        if (jobName == null) {
            throw new AbortException("'jobName' parameter not set!");
        }

        Job<?, ?> upstreamJob = Jenkins.getActiveInstance().getItem(jobName, run.getParent(), Job.class);
        if (upstreamJob == null) {
            throw new AbortException(format("Can't find upstream project named: '%s'", jobName));
        }

        String permalink = step.getPermalink();
        if (permalink != null) {
            PermalinkProjectAction.Permalink p = upstreamJob.getPermalinks().get(permalink);
            if (p == null) {
                throw new AbortException(format("Can't find run in the upstream job '%s' identified by '%s'", upstreamJob, permalink));
            }
            Run<?, ?> upstreamRun = p.resolve(upstreamJob);
            if (upstreamRun == null) {
                throw new AbortException("Can't find run by permalink!");
            }
            return new RunWrapper(upstreamRun, false);
        }

        Integer buildNumber = step.getBuildNumber();
        if (buildNumber != null) {
            Run<?, ?> upstreamRun = upstreamJob.getBuildByNumber(buildNumber);
            if (upstreamRun == null) {
                throw new AbortException("Can't find run by build number!");
            }
            return new RunWrapper(upstreamRun, false);
        }

        listener.getLogger().println("Fallback to searching for the last stable build");
        Run<?, ?> upstreamRun = upstreamJob.getLastStableBuild();
        if (upstreamRun == null) {
            throw new AbortException("Can't find last stable build!");
        }
        return new RunWrapper(upstreamRun, false);
    }
}
