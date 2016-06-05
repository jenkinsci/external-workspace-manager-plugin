package org.jenkinsci.plugins.ewm.strategies;

import hudson.AbortException;
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
    public Disk allocateDisk(List<Disk> disks) throws AbortException {
        Iterator<Disk> iterator = disks.iterator();
        Disk selectedDisk = iterator.next();
        long selectedDiskUsableSpace = retrieveUsableSpace(selectedDisk);

        while (iterator.hasNext()) {
            Disk disk = iterator.next();
            long diskUsableSpace = retrieveUsableSpace(disk);

            if (diskUsableSpace > selectedDiskUsableSpace) {
                selectedDisk = disk;
                selectedDiskUsableSpace = diskUsableSpace;
            }
        }

        return selectedDisk;
    }

    /**
     * Calculates the usable space for the given {@link Disk} entry.
     *
     * @param disk the disk entry
     * @return the usable space for the disk
     * @throws AbortException if mounting point from Master to Disk is {@code null}
     */
    private long retrieveUsableSpace(Disk disk) throws AbortException {
        String masterMountPoint = disk.getMasterMountPoint();
        if (masterMountPoint == null) {
            String message = String.format("Mounting point from Master to the disk is not defined for Disk ID '%s', from Disk Pool ID '%s'", disk.getDiskId(), diskPoolId);
            throw new AbortException(message);
        }

        return new File(masterMountPoint).getUsableSpace();
    }
}
