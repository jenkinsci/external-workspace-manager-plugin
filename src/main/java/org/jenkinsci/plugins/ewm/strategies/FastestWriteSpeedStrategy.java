package org.jenkinsci.plugins.ewm.strategies;

import hudson.Extension;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategyDescriptor;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Selects the {@link Disk} with the highest {@link org.jenkinsci.plugins.ewm.DiskInfoProvider#writeSpeed}.
 *
 * @author Alexandru Somai
 */
public class FastestWriteSpeedStrategy extends DiskAllocationStrategy {

    @DataBoundConstructor
    public FastestWriteSpeedStrategy() {
    }

    @Nonnull
    @Override
    public Disk allocateDisk(@Nonnull List<Disk> disks) throws IOException {
        return Collections.max(disks, new Comparator<Disk>() {

            @Override
            public int compare(Disk disk1, Disk disk2) {
                Double disk1WriteSpeed = disk1.getDiskInfo().getWriteSpeed();
                Double disk2WriteSpeed = disk2.getDiskInfo().getWriteSpeed();

                if (disk1WriteSpeed == null || disk2WriteSpeed == null) {
                    return 0;
                }
                return Double.compare(disk1WriteSpeed, disk2WriteSpeed);
            }
        });
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
