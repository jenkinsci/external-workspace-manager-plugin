package org.jenkinsci.plugins.ewm.providers;

import hudson.Extension;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.DiskInfoProviderDescriptor;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * @author Alexandru Somai
 */
public class NoDiskInfo extends DiskInfoProvider {

    @DataBoundConstructor
    public NoDiskInfo() {
        super(null, null);
    }

    @Extension
    public static class DescriptorImpl extends DiskInfoProviderDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.providers_NoDiskInfo_DisplayName();
        }
    }
}
