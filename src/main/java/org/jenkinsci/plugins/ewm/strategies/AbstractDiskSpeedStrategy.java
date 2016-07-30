package org.jenkinsci.plugins.ewm.strategies;

import org.jenkinsci.plugins.ewm.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.definitions.Disk;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Base class that selects the {@link Disk} with the highest speed provided by the
 * {@link #getDiskSpeed(DiskInfoProvider)} method
 *
 * @author Alexandru Somai
 */
public abstract class AbstractDiskSpeedStrategy extends DiskAllocationStrategy {

    @Nonnull
    @Override
    public Disk allocateDisk(@Nonnull List<Disk> disks) throws IOException {
        return Collections.max(disks, new Comparator<Disk>() {

            @Override
            public int compare(Disk disk1, Disk disk2) {
                Double disk1Speed = getDiskSpeed(disk1.getDiskInfo());
                Double disk2Speed = getDiskSpeed(disk2.getDiskInfo());

                if (disk1Speed == null || disk2Speed == null) {
                    return 0;
                }
                return Double.compare(disk1Speed, disk2Speed);
            }
        });
    }

    /**
     * Override this method to return the speed property on which the selection should be made.
     *
     * @param diskInfo the {@link DiskInfoProvider} that contains R/W {@link Disk} speed
     * @return the value represented by the R/W speed
     */
    @CheckForNull
    protected abstract Double getDiskSpeed(@Nonnull DiskInfoProvider diskInfo);
}
