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
 * Selects the {@link Disk} with the highest {@link org.jenkinsci.plugins.ewm.DiskInfoProvider#readSpeed}.
 *
 * @author Alexandru Somai
 */
public class FastestReadSpeedStrategy extends DiskAllocationStrategy {

    @DataBoundConstructor
    public FastestReadSpeedStrategy() {
    }

    @Nonnull
    @Override
    public Disk allocateDisk(@Nonnull List<Disk> disks) throws IOException {
        return Collections.max(disks, new Comparator<Disk>() {

            @Override
            public int compare(Disk disk1, Disk disk2) {
                Double disk1ReadSpeed = disk1.getDiskInfo().getReadSpeed();
                Double disk2ReadSpeed = disk2.getDiskInfo().getReadSpeed();

                if (disk1ReadSpeed == null || disk2ReadSpeed == null) {
                    return 0;
                }
                return Double.compare(disk1ReadSpeed, disk2ReadSpeed);
            }
        });
    }

    @Extension
    public static class DescriptorImpl extends DiskAllocationStrategyDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.strategies_FastestReadSpeed_DisplayName();
        }
    }
}
