package org.jenkinsci.plugins.ewm.definitions;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.nodes.DiskNode;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.Util.fixNull;
import static hudson.util.FormValidation.validateRequired;

/**
 * Describable used in the Jenkins global config.
 * Based on a template, the user may define similar {@link DiskNode} properties to be used
 * for multiple {@link hudson.model.Node}s that have a common {@link Template#label}.
 *
 * @author Alexandru Somai
 */
public class Template implements Describable<Template> {

    private final String diskPoolRefId;
    private final String label;
    private final List<DiskNode> diskNodes;

    @DataBoundConstructor
    public Template(String diskPoolRefId, String label, List<DiskNode> diskNodes) {
        this.diskPoolRefId = fixEmptyAndTrim(diskPoolRefId);
        this.label = fixEmptyAndTrim(label);
        this.diskNodes = fixNull(diskNodes);
    }

    @Override
    public TemplateDescriptor getDescriptor() {
        return (TemplateDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    @CheckForNull
    public String getDiskPoolRefId() {
        return diskPoolRefId;
    }

    @CheckForNull
    public String getLabel() {
        return label;
    }

    @Nonnull
    public List<DiskNode> getDiskNodes() {
        return diskNodes;
    }

    @Extension
    public static class TemplateDescriptor extends Descriptor<Template> {

        public FormValidation doCheckDiskPoolRefId(@QueryParameter String value) {
            return validateRequired(value);
        }

        public FormValidation doCheckLabel(@QueryParameter String value) {
            return validateRequired(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.definitions_Template_DisplayName();
        }
    }
}
