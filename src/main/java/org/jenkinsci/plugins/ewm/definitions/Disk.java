package org.jenkinsci.plugins.ewm.definitions;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.Util.isRelativePath;
import static hudson.util.FormValidation.validateRequired;

/**
 * Describable used to define the disk information within a {@link DiskPool}.
 *
 * @author Alexandru Somai
 */
public class Disk implements Describable<Disk> {

    private final String diskId;
    private final String displayName;
    private final String masterMountPoint;
    private final String physicalPathOnDisk;

    @CheckForNull
    private DiskInfoProvider diskInfo;

    @DataBoundConstructor
    public Disk(String diskId, String displayName, String masterMountPoint, String physicalPathOnDisk) {
        this.diskId = fixEmptyAndTrim(diskId);
        this.displayName = fixEmptyAndTrim(displayName);
        this.masterMountPoint = fixEmptyAndTrim(masterMountPoint);
        this.physicalPathOnDisk = fixEmptyAndTrim(physicalPathOnDisk);
    }

    @Override
    public Descriptor<Disk> getDescriptor() {
        return DESCRIPTOR;
    }

    @CheckForNull
    public String getDiskId() {
        return diskId;
    }

    @CheckForNull
    public String getDisplayName() {
        return displayName != null ? displayName : diskId;
    }

    @CheckForNull
    public String getMasterMountPoint() {
        return masterMountPoint;
    }

    @CheckForNull
    public String getPhysicalPathOnDisk() {
        return physicalPathOnDisk;
    }

    @CheckForNull
    public DiskInfoProvider getDiskInfo() {
        return diskInfo;
    }

    @DataBoundSetter
    public void setDiskInfo(DiskInfoProvider diskInfo) {
        this.diskInfo = diskInfo;
    }

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
                return FormValidation.error("Must be a relative path");
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
