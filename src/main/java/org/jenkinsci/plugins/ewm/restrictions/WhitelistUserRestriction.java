package org.jenkinsci.plugins.ewm.restrictions;

import hudson.Extension;
import org.jenkinsci.plugins.ewm.DiskPoolRestrictionDescriptor;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * @author Alexandru Somai
 */
public class WhitelistUserRestriction extends AbstractUserRestriction {

    // TODO add whitelist users list

    @DataBoundConstructor
    public WhitelistUserRestriction() {
    }

    @Override
    protected boolean acceptUser(@Nonnull String userId) {
        // TODO add logic
        return true;
    }

    @Extension
    public static class DescriptorImpl extends DiskPoolRestrictionDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.restrictions_WhitelistUserRestriction_DisplayName();
        }
    }
}
