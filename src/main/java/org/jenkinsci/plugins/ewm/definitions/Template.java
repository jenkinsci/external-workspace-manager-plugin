package org.jenkinsci.plugins.ewm.definitions;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.nodes.NodeDiskPool;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.Util.fixNull;
import static hudson.util.FormValidation.validateRequired;

/**
 * Describable used in the Jenkins global config.
 * Based on a template, the user may define similar {@link NodeDiskPool} properties to be used
 * for multiple {@link hudson.model.Node}s that have a common {@link Template#label}.
 *
 * @author Alexandru Somai
 */
public class Template implements Describable<Template> {

    private final String label;
    private final List<NodeDiskPool> nodeDiskPools;

    @DataBoundConstructor
    public Template(String label, List<NodeDiskPool> nodeDiskPools) {
        this.label = fixEmptyAndTrim(label);
        this.nodeDiskPools = fixNull(nodeDiskPools);
    }

    @CheckForNull
    public String getLabel() {
        return label;
    }

    @Nonnull
    public List<NodeDiskPool> getNodeDiskPools() {
        return Collections.unmodifiableList(nodeDiskPools);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<Template> {

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
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
