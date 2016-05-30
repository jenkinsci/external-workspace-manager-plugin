package org.jenkinsci.plugins.ewm.definitions;

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
import java.util.ArrayList;
import java.util.List;

import static hudson.Util.fixEmptyAndTrim;

/**
 * Describable for defining a disk pool information in the Jenkins global config.
 * Each disk pool should have at least one {@link Disk} entry.
 *
 * @author Alexandru Somai
 */
public class DiskPool implements Describable<DiskPool> {

    private final String diskPoolId;
    private final String name;
    private final String description;
    private final List<Disk> disks;

    @DataBoundConstructor
    public DiskPool(String diskPoolId, String name, String description, List<Disk> disks) {
        this.diskPoolId = fixEmptyAndTrim(diskPoolId);
        this.name = fixEmptyAndTrim(name);
        this.description = fixEmptyAndTrim(description);
        this.disks = disks != null ? disks : new ArrayList<Disk>();
    }

    @Override
    public DiskPoolDescriptor getDescriptor() {
        return (DiskPoolDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    @CheckForNull
    public String getDiskPoolId() {
        return diskPoolId;
    }

    @CheckForNull
    public String getName() {
        return name;
    }

    @CheckForNull
    public String getDescription() {
        return description;
    }

    public List<Disk> getDisks() {
        return disks;
    }

    @Extension
    public static class DiskPoolDescriptor extends Descriptor<DiskPool> {

        public FormValidation doCheckDiskPoolId(@QueryParameter String value) {
            return FormValidationUtil.doCheckValue(value);
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            return FormValidationUtil.doCheckValue(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.definitions_DiskPool_DisplayName();
        }
    }
}
