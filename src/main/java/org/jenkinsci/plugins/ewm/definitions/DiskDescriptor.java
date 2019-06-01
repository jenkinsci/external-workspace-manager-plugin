package org.jenkinsci.plugins.ewm.definitions;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;

import static hudson.Util.isRelativePath;
import static hudson.util.FormValidation.validateRequired;

public abstract class DiskDescriptor extends Descriptor<Disk> {

    public DiskDescriptor() {
        super();
    }

    /**
     * @param clazz class to describe
     * @see Descriptor#Descriptor(Class)
     */
    public DiskDescriptor(Class<? extends Disk> clazz) {
        super(clazz);
    }

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
}
