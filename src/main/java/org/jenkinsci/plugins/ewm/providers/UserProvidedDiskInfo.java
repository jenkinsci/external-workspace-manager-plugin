package org.jenkinsci.plugins.ewm.providers;

import hudson.Extension;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.DiskInfoProviderDescriptor;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.utils.FormValidationUtil;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;

/**
 * @author Alexandru Somai
 */
public class UserProvidedDiskInfo extends DiskInfoProvider {

    @DataBoundConstructor
    public UserProvidedDiskInfo(double readSpeed, double writeSpeed) {
        super(readSpeed, writeSpeed);
    }

    @Extension
    public static class DescriptorImpl extends DiskInfoProviderDescriptor {

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckReadSpeed(@QueryParameter String value) {
            return FormValidationUtil.validatePositiveDouble(value);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckWriteSpeed(@QueryParameter String value) {
            return FormValidationUtil.validatePositiveDouble(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.providers_UserProvidedDiskInfo_DisplayName();
        }
    }
}
