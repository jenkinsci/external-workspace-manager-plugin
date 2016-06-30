package org.jenkinsci.plugins.ewm.definitions;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static hudson.Util.*;
import static hudson.util.FormValidation.validateRequired;

/**
 * Describable used to define the disk information within a {@link DiskPool}.
 *
 * @author Alexandru Somai
 */
public class Disk implements Describable<Disk> {

    private final String diskId;
    private final String name;
    private final String masterMountPoint;
    private final String physicalPathOnDisk;

    @DataBoundConstructor
    public Disk(String diskId, String name, String masterMountPoint, String physicalPathOnDisk) {
        this.diskId = fixEmptyAndTrim(diskId);
        this.name = fixEmptyAndTrim(name);
        this.masterMountPoint = fixEmptyAndTrim(masterMountPoint);
        this.physicalPathOnDisk = fixNull(physicalPathOnDisk).trim();
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
    public String getName() {
        return name != null ? name : diskId;
    }

    @CheckForNull
    public String getMasterMountPoint() {
        return masterMountPoint;
    }

    @Nonnull
    public String getPhysicalPathOnDisk() {
        return physicalPathOnDisk;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<Disk> {

        public FormValidation doCheckDiskId(@QueryParameter String value) {
            return validateRequired(value);
        }

        public FormValidation doCheckMasterMountPoint(@QueryParameter String value) {
            return validateRequired(value);
        }

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
