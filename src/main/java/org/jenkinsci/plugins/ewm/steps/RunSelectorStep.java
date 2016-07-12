package org.jenkinsci.plugins.ewm.steps;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;

/**
 * @author Alexandru Somai
 */
public class RunSelectorStep extends AbstractStepImpl {

    @CheckForNull
    private final String jobName;
    @CheckForNull
    private String permalink;
    @CheckForNull
    private Integer buildNumber;

    // TODO add a config file for this!

    @DataBoundConstructor
    public RunSelectorStep(String jobName) {
        this.jobName = jobName;
    }

    @CheckForNull
    public String getJobName() {
        return jobName;
    }

    @CheckForNull
    public String getPermalink() {
        return permalink;
    }

    @DataBoundSetter
    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    @CheckForNull
    public Integer getBuildNumber() {
        return buildNumber;
    }

    @DataBoundSetter
    public void setBuildNumber(Integer buildNumber) {
        this.buildNumber = buildNumber;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(RunSelectorExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "runSelector";
        }
    }
}
