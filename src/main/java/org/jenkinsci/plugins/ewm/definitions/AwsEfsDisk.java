package org.jenkinsci.plugins.ewm.definitions;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.providers.NoDiskInfo;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.Util.isRelativePath;
import static hudson.util.FormValidation.validateRequired;

public class AwsEfsDisk extends Disk {
    private final AWSCredentialsProvider
    @DataBoundConstructor
    public AwsEfsDisk(String diskId, String displayName, String masterMountPoint,
                      String physicalPathOnDisk, DiskInfoProvider diskInfo) {
        super(diskId, displayName, masterMountPoint, physicalPathOnDisk, diskInfo);
    }



    // TODO : do i need to implement this method ?
    @Override
    public Descriptor<Disk> getDescriptor() {
        return DESCRIPTOR;
    }

    // TODO : is this an extension point ? Do I need to add it manually ?
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<Disk> {

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckDiskId(@QueryParameter String value) {
            return validateRequired(value);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckMasterMountPoint(@QueryParameter String value) {
            return validateRequired(value);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckPhysicalPathOnDisk(@QueryParameter String value) {
            if (!isRelativePath(value)) {
                return FormValidation.error(Messages.formValidation_NotRelativePath());
            }
            return FormValidation.ok();
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.definitions_Disk_DisplayName();
        }
    }
}
