package org.jenkinsci.plugins.ewm.steps;

import hudson.Extension;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.definitions.Template;
import org.jenkinsci.plugins.ewm.steps.model.ExternalWorkspace;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * The 'exws' step.
 * Allocates the final external workspace on the current node and uses that as the default directory for nested steps.
 *
 * @author Alexandru Somai
 */
public class ExwsStep extends AbstractStepImpl {

    private final ExternalWorkspace externalWorkspace;

    @DataBoundConstructor
    public ExwsStep(ExternalWorkspace externalWorkspace) {
        this.externalWorkspace = externalWorkspace;
    }

    @CheckForNull
    public ExternalWorkspace getExternalWorkspace() {
        return externalWorkspace;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        private List<Template> templates = new ArrayList<>();

        public DescriptorImpl() {
            super(ExwsExecution.class);
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            templates = req.bindJSONToList(Template.class, formData.get("templates"));
            save();
            return super.configure(req, formData);
        }

        @Nonnull
        public List<Template> getTemplates() {
            return templates;
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
