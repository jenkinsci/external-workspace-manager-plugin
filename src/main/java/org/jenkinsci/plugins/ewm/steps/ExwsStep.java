package org.jenkinsci.plugins.ewm.steps;

import hudson.Extension;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.ewm.definitions.Template;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Alexandru Somai
 *         date 5/25/16
 */
public class ExwsStep extends AbstractStepImpl implements Serializable {

    private final String path;

    @DataBoundConstructor
    public ExwsStep(String path) {
        this.path = path;
    }

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

        public static List<Template.TemplateDescriptor> getDescriptors() {
            return Jenkins.getInstance().getDescriptorList(Template.class);
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
            return "Use external workspace";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }
}
