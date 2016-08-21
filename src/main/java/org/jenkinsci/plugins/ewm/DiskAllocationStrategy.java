package org.jenkinsci.plugins.ewm;

import hudson.AbortException;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Abstract class for defining a disk allocation strategy.
 * Each disk allocation strategy should extend this class and provide its own implementation of disk allocation.
 *
 * @author Alexandru Somai
 */
public abstract class DiskAllocationStrategy extends AbstractDescribableImpl<DiskAllocationStrategy> implements ExtensionPoint {

    private static final long MEGABYTE = 1024L * 1024L;

    // estimated workspace size has to be in MB
    private long estimatedWorkspaceSize;

    /**
     * @return all registered {@link DiskAllocationStrategy}s.
     */
    @Nonnull
    public static ExtensionList<DiskAllocationStrategy> all() {
        return ExtensionList.lookup(DiskAllocationStrategy.class);
    }

    /**
     * @return the registered {@link DiskAllocationStrategyDescriptor}s for the {@link DiskAllocationStrategy}.
     */
    @Nonnull
    public static ExtensionList<DiskAllocationStrategyDescriptor> allDescriptors() {
        return ExtensionList.lookup(DiskAllocationStrategyDescriptor.class);
    }

    /**
     * Allocates a disk from the given list. The list contains at least one {@link Disk} entry.
     *
     * @param disks the entries from which to allocate a disk. The list has at least one element
     * @return the selected disk
     * @throws IOException if any mandatory field is missing from the {@link Disk} entry,
     *                     or if the disk allocation fails for any reason
     */
    @Nonnull
    public abstract Disk allocateDisk(@Nonnull List<Disk> disks) throws IOException;

    /**
     * Retrieves the usable space in bytes for the given {@link Disk} entry.
     * It uses the mounting point property that is defined in the Jenkins global config for each Disk.
     *
     * @param disk the disk entry
     * @return the disk's usable space in bytes
     * @throws IOException if mounting point from Jenkins master to Disk is {@code null}, or
     *                     if the usable space can't be retrieved for security reasons
     * @see File#getUsableSpace
     */
    @Restricted(NoExternalUse.class)
    public long retrieveUsableSpaceInBytes(Disk disk) throws IOException {
        String masterMountPoint = disk.getMasterMountPoint();
        if (masterMountPoint == null) {
            String message = String.format("Mounting point from Master to the disk is not defined for Disk ID '%s'", disk.getDiskId());
            throw new AbortException(message);
        }

        try {
            return new File(masterMountPoint).getUsableSpace();
        } catch (SecurityException e) {
            throw new AbortException(String.format("Can't retrieve usable space for Disk ID '%s' because of security reasons", disk.getDiskId()));
        }
    }

    /**
     * Retrieves the usable space in MB for the given {@link Disk} entry.
     * It converts the value returned by {@link #retrieveUsableSpaceInBytes(Disk)} to MB.
     *
     * @param disk the disk entry
     * @return the disk's usable space in MB
     * @throws IOException same as {@link #retrieveUsableSpaceInBytes(Disk)}
     * @see #retrieveUsableSpaceInBytes(Disk)
     * @see #bytesToMega(long)
     */
    @Restricted(NoExternalUse.class)
    protected final long retrieveUsableSpaceInMegaBytes(Disk disk) throws IOException {
        return bytesToMega(retrieveUsableSpaceInBytes(disk));
    }

    /**
     * Converts the given bytes value to megabytes.
     * The formula used is bytes / (1024 * 1024).
     *
     * @param bytes the given value in bytes
     * @return the converted value to megabytes
     */
    private static long bytesToMega(long bytes) {
        return bytes / MEGABYTE;
    }

    /**
     * Returns the estimated workspace size in MB.
     *
     * @return the estimated workspace size in MB
     */
    public long getEstimatedWorkspaceSize() {
        return estimatedWorkspaceSize;
    }

    /**
     * Sets the estimated workspace size.
     * It must be set in MB.
     *
     * @param estimatedWorkspaceSize the estimated workspace size in MB
     */
    public void setEstimatedWorkspaceSize(long estimatedWorkspaceSize) {
        this.estimatedWorkspaceSize = estimatedWorkspaceSize;
    }
}
