package org.jenkinsci.plugins.ewm.restrictions;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.ewm.DiskPoolRestriction;
import org.jenkinsci.plugins.ewm.DiskPoolRestrictionDescriptor;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * @author Alexandru Somai
 */
public class NoDiskPoolRestriction extends DiskPoolRestriction {

    @DataBoundConstructor
    public NoDiskPoolRestriction() {
    }

    @Override
    public boolean isAllowed(@Nonnull Run<?, ?> run, @Nonnull TaskListener listener) {
        return true;
    }

    @Extension
    public static class DescriptorImpl extends DiskPoolRestrictionDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.restrictions_NoDiskPoolRestriction_DisplayName();
        }
    }
}
