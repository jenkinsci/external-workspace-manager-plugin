package org.jenkinsci.plugins.ewm.providers;

import hudson.Extension;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.DiskInfoProviderDescriptor;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;

/**
 * @author Alexandru Somai
 */
public class UserProvidedDiskInfo extends DiskInfoProvider {

    @DataBoundConstructor
    public UserProvidedDiskInfo(String name, String type, Long size, Double writeSpeed, Double readSpeed) {
        super(name, type, size, writeSpeed, readSpeed);
    }

    @Extension
    public static class DescriptorImpl extends DiskInfoProviderDescriptor {

        @SuppressWarnings("unused")
        public FormValidation doCheckName(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckType(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckSize(@QueryParameter String value) {
            try {
                long result = Long.parseLong(value);
                if (result <= 0) {
                    return FormValidation.error("Must be positive");
                }
            } catch (NumberFormatException e) {
                return FormValidation.error("Must be a long value");
            }

            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckWriteSpeed(@QueryParameter String value) {
            return validateDoubleValue(value);
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckReadSpeed(@QueryParameter String value) {
            return validateDoubleValue(value);
        }

        private static FormValidation validateDoubleValue(String value) {
            try {
                double result = Double.parseDouble(value);
                if (result <= 0) {
                    return FormValidation.error("Must be positive");
                }
            } catch (NumberFormatException e) {
                return FormValidation.error("Must be a double value");
            }

            return FormValidation.ok();
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.providers_UserProvidedDiskInfo_DisplayName();
        }
    }
}
