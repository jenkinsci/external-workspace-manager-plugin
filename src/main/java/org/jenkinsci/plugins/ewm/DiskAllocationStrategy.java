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
     * @return the usable space in bytes for the disk
     * @throws IOException if mounting point from Jenkins master to Disk is {@code null}, or
     *                     if the usable space can't be retrieved for security reasons
     * @see File#getUsableSpace
     */
    @Restricted(NoExternalUse.class)
    public long retrieveUsableSpace(Disk disk) throws IOException {
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

    public long getEstimatedWorkspaceSize() {
        return estimatedWorkspaceSize;
    }

    public void setEstimatedWorkspaceSize(long estimatedWorkspaceSize) {
        this.estimatedWorkspaceSize = estimatedWorkspaceSize;
    }

    /**
     * Assuming that the {@link #estimatedWorkspaceSize} is provided by the user in MB,
     * this method returns the size in bytes (multiplied by 1024 * 1024).
     *
     * @return the estimated workspace size in bytes (multiplied by 1024 * 1024)
     */
    public long getEstimatedWorkspaceSizeInBytes() {
        return estimatedWorkspaceSize * 1024 * 1024;
    }
}
