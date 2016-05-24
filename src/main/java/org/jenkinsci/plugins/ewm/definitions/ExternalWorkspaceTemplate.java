package org.jenkinsci.plugins.ewm.definitions;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Alexandru Somai
 *         date 5/24/16
 */
public class ExternalWorkspaceTemplate implements Describable<ExternalWorkspaceTemplate> {

    private final List<Template> templates;

    @DataBoundConstructor
    public ExternalWorkspaceTemplate(List<Template> templates) {
        this.templates = templates;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    public List<Template> getTemplates() {
        return templates;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ExternalWorkspaceTemplate> {

        private List<Template> templates;

        public DescriptorImpl() {
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

        @Nonnull
        @Override
        public String getDisplayName() {
            return "External Workspace Template";
        }

        public List<Template> getTemplates() {
            return templates;
        }

        public void setTemplates(List<Template> templates) {
            this.templates = templates;
        }
    }
}
