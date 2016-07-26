package org.jenkinsci.plugins.ewm.nodes;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.util.FormValidation.validateRequired;

/**
 * Describable used to define disk properties for each {@link hudson.model.Node} configuration.
 *
 * @author Alexandru Somai
 */
public class DiskNode implements Describable<DiskNode> {

    private final String diskRefId;
    private final String localRootPath;

    @DataBoundConstructor
    public DiskNode(String diskRefId, String localRootPath) {
        this.diskRefId = fixEmptyAndTrim(diskRefId);
        this.localRootPath = fixEmptyAndTrim(localRootPath);
    }

    @CheckForNull
    public String getDiskRefId() {
        return diskRefId;
    }

    @CheckForNull
    public String getLocalRootPath() {
        return localRootPath;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<DiskNode> {

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckDiskRefId(@QueryParameter String value) {
            return validateRequired(value);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckLocalRootPath(@QueryParameter String value) {
            return validateRequired(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.definitions_Disk_DisplayName();
        }
    }
}
