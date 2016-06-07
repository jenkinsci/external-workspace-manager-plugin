package org.jenkinsci.plugins.ewm.strategies;

import hudson.AbortException;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Abstract class for defining a disk allocation strategy.
 * Each disk allocation strategy should extend this class and provide its own implementation of disk allocation.
 *
 * @author Alexandru Somai
 */
public abstract class DiskAllocationStrategy {

    private final String diskPoolId;
    private final List<DiskPool> diskPools;

    protected DiskAllocationStrategy(@Nonnull String diskPoolId, @Nonnull List<DiskPool> diskPools) {
        this.diskPoolId = diskPoolId;
        this.diskPools = diskPools;
    }

    /**
     * Allocates a disk from the given disk pool.
     *
     * @return the allocated disk pool
     * @throws IOException if the disk pool doesn't have defined any {@link Disk} entries
     * @see DiskAllocationStrategy#findDiskPool()
     * @see DiskAllocationStrategy#allocateDisk(List) ()
     */
    @Nonnull
    public Disk allocateDisk() throws IOException {
        DiskPool diskPool = findDiskPool();

        List<Disk> disks = diskPool.getDisks();
        if (disks.isEmpty()) {
            String message = String.format("No Disks were defined in the global config for Disk Pool '%s'", diskPoolId);
            throw new AbortException(message);
        }

        return allocateDisk(disks);
    }

    /**
     * Iterates through the defined {@link DiskAllocationStrategy#diskPools} and finds the {@link DiskPool} that has
     * the id equal to {@link DiskAllocationStrategy#diskPoolId}.
     *
     * @return the disk pool whose id is equal to {@link DiskAllocationStrategy#diskPoolId}
     * @throws IOException if there isn't find any disk pool matching the disk pool id
     */
    @Nonnull
    private DiskPool findDiskPool() throws IOException {
        DiskPool diskPool = null;
        for (DiskPool dp : diskPools) {
            if (diskPoolId.equals(dp.getDiskPoolId())) {
                diskPool = dp;
                break;
            }
        }
        if (diskPool == null) {
            String message = String.format("No Disk Pool ID matching '%s' was found in the global config", diskPoolId);
            throw new AbortException(message);
        }

        return diskPool;
    }

    /**
     * Allocates a disk from the given list. The list contains at least one {@link Disk} entry.
     *
     * @param disks the disks from which to allocate a disk. The list has at least one element.
     * @return the selected disk
     * @throws IOException if any mandatory field is missing from the {@link Disk} entry
     */
    @Nonnull
    protected abstract Disk allocateDisk(List<Disk> disks) throws IOException;

    protected String getDiskPoolId() {
        return diskPoolId;
    }
}
