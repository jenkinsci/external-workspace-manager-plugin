package org.jenkinsci.plugins.ewm.definitions;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.utils.FormValidationUtil;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.Util.fixNull;
import static hudson.util.FormValidation.validateRequired;

/**
 * Describable for defining a disk pool information in the Jenkins global config.
 * Each disk pool should have at least one {@link Disk} entry.
 *
 * @author Alexandru Somai
 */
public class DiskPool implements Describable<DiskPool> {

    private final String diskPoolId;
    private final String displayName;
    private final String description;
    private final String workspaceTemplate;
    private final List<Disk> disks;

    @CheckForNull
    private JobRestriction restriction;

    @DataBoundConstructor
    public DiskPool(String diskPoolId, String displayName, String description, String workspaceTemplate, List<Disk> disks) {
        this.diskPoolId = fixEmptyAndTrim(diskPoolId);
        this.displayName = fixEmptyAndTrim(displayName);
        this.description = fixEmptyAndTrim(description);
        this.workspaceTemplate = fixEmptyAndTrim(workspaceTemplate);
        this.disks = fixNull(disks);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @CheckForNull
    public String getDiskPoolId() {
        return diskPoolId;
    }

    @CheckForNull
    public String getDisplayName() {
        return displayName != null ? displayName : diskPoolId;
    }

    @CheckForNull
    public String getDescription() {
        return description;
    }

    @CheckForNull
    public String getWorkspaceTemplate() {
        return workspaceTemplate;
    }

    @Nonnull
    public List<Disk> getDisks() {
        return disks;
    }

    @CheckForNull
    public JobRestriction getRestriction() {
        return restriction;
    }

    @DataBoundSetter
    public void setRestriction(JobRestriction restriction) {
        this.restriction = restriction;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<DiskPool> {

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckDiskPoolId(@QueryParameter String value) {
            return validateRequired(value);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckWorkspaceTemplate(@QueryParameter String value) {
            return FormValidationUtil.validateWorkspaceTemplate(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.definitions_DiskPool_DisplayName();
        }
    }
}
