package org.jenkinsci.plugins.ewm.strategies;

import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * {@link DiskAllocationStrategy} implementation that allocates the disk with the most usable space.
 *
 * @author Alexandru Somai
 */
public class MostUsableSpaceStrategy extends DiskAllocationStrategy {

    public MostUsableSpaceStrategy(@Nonnull String diskPoolId, @Nonnull List<DiskPool> diskPools) {
        super(diskPoolId, diskPools);
    }

    @Nonnull
    @Override
    public Disk allocateDisk(List<Disk> disks) {
        Iterator<Disk> iterator = disks.iterator();
        Disk selectedDisk = iterator.next();
        long selectedDiskUsableSpace = new File(selectedDisk.getMasterMountPoint()).getUsableSpace();

        while (iterator.hasNext()) {
            Disk disk = iterator.next();
            long diskUsableSpace = new File(disk.getMasterMountPoint()).getUsableSpace();

            if (diskUsableSpace > selectedDiskUsableSpace) {
                selectedDisk = disk;
                selectedDiskUsableSpace = diskUsableSpace;
            }
        }

        return selectedDisk;
    }
}
