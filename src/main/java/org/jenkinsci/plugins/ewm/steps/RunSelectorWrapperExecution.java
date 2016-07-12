package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;

import static java.lang.String.format;

/**
 * @author Alexandru Somai
 */
public class RunSelectorWrapperExecution extends AbstractSynchronousNonBlockingStepExecution<RunWrapper> {

    @Inject
    private transient RunSelectorWrapperStep step;

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

        RunSelector selector = step.getSelector();
        if (selector == null) {
            throw new AbortException("Run Selector not set!");
        }

        RunSelectorContext context = new RunSelectorContext(Jenkins.getActiveInstance(), run, listener);
        context.setVerbose(true);

        Run<?, ?> upstreamRun = selector.select(upstreamJob, context);
        if (upstreamRun == null) {
            throw new AbortException("No upstream run found!");
        }

        return new RunWrapper(upstreamRun, false);
    }
}
