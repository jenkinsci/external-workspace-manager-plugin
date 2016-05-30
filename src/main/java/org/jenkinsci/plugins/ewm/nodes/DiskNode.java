package org.jenkinsci.plugins.ewm.nodes;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.util.FormValidationUtil;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static hudson.Util.fixEmptyAndTrim;

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
    public DiskNodeDescriptor getDescriptor() {
        return (DiskNodeDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    @Extension
    public static class DiskNodeDescriptor extends Descriptor<DiskNode> {

        public FormValidation doCheckDiskRefId(@QueryParameter String value) {
            return FormValidationUtil.doCheckValue(value);
        }

        public FormValidation doCheckLocalRootPath(@QueryParameter String value) {
            return FormValidationUtil.doCheckValue(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.definitions_Disk_DisplayName();
        }
    }
}
