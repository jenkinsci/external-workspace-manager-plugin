package org.jenkinsci.plugins.ewm.strategies;

import hudson.Extension;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategyDescriptor;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Selects the {@link Disk} with the highest {@link org.jenkinsci.plugins.ewm.DiskInfoProvider#writeSpeed}.
 *
 * @author Alexandru Somai
 */
public class FastestWriteSpeedStrategy extends AbstractDiskSpeedStrategy {

    @DataBoundConstructor
    public FastestWriteSpeedStrategy() {
    }

    @CheckForNull
    @Override
    protected Double getDiskSpeed(@Nonnull DiskInfoProvider diskInfo) {
        return diskInfo.getWriteSpeed();
    }

    @Extension
    public static class DescriptorImpl extends DiskAllocationStrategyDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.strategies_FastestWriteSpeed_DisplayName();
        }
    }
}
