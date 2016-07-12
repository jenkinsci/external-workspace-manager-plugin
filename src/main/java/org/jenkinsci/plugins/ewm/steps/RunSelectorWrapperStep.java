package org.jenkinsci.plugins.ewm.steps;

import hudson.Extension;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;

/**
 * @author Alexandru Somai
 */
public class RunSelectorWrapperStep extends AbstractStepImpl {

    @CheckForNull
    private final String jobName;
    @CheckForNull
    private RunSelector selector;

    // TODO add a config file for this!

    @DataBoundConstructor
    public RunSelectorWrapperStep(String jobName) {
        this.jobName = jobName;
    }

    @CheckForNull
    public String getJobName() {
        return jobName;
    }

    public RunSelector getSelector() {
        return selector;
    }

    @DataBoundSetter
    public void setSelector(RunSelector selector) {
        this.selector = selector;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(RunSelectorWrapperExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "runSelectorWrapper";
        }
    }
}
