package org.jenkinsci.plugins.ewm.steps;

import hudson.Extension;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.definitions.Template;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

import static hudson.Util.fixEmptyAndTrim;

/**
 * TODO - To be added when I'll implement the step
 *
 * @author Alexandru Somai
 */
public class ExwsStep extends AbstractStepImpl implements Serializable {

    private final String path;

    @DataBoundConstructor
    public ExwsStep(String path) {
        this.path = fixEmptyAndTrim(path);
    }

    @CheckForNull
    public String getPath() {
        return path;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        private List<Template> templates;

        public DescriptorImpl() {
            super(ExwsExecution.class);
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindJSON(this, formData);
            save();
            return super.configure(req, formData);
        }

        public List<Template> getTemplates() {
            return templates;
        }

        public void setTemplates(List<Template> templates) {
            this.templates = templates;
        }

        @Override
        public String getFunctionName() {
            return "exws";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.steps_ExwsStep_DisplayName();
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }
}
