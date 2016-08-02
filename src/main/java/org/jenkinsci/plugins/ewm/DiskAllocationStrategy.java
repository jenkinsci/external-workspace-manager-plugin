package org.jenkinsci.plugins.ewm;

import hudson.AbortException;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import org.jenkinsci.plugins.ewm.definitions.Disk;

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
     * Allocates a disk from the given list. The list contains at least one {@link Disk} entry.
     *
     * @param disks the entries from which to allocate a disk. The list has at least one element
     * @return the selected disk
     * @throws IOException if any mandatory field is missing from the {@link Disk} entry
     */
    @Nonnull
    public abstract Disk allocateDisk(@Nonnull List<Disk> disks) throws IOException;

    /**
     * Calculates the usable space for the given {@link Disk} entry.
     * It uses the mounting point property that is defined in the Jenkins global config for each Disk.
     *
     * @param disk the disk entry
     * @return the usable space for the disk
     * @throws IOException if mounting point from Jenkins Master to Disk is {@code null}
     */
    protected long retrieveUsableSpace(Disk disk) throws IOException {
        String masterMountPoint = disk.getMasterMountPoint();
        if (masterMountPoint == null) {
            String message = String.format("Mounting point from Master to the disk is not defined for Disk ID '%s'", disk.getDiskId());
            throw new AbortException(message);
        }

        return new File(masterMountPoint).getUsableSpace();
    }

    public long getEstimatedWorkspaceSize() {
        return estimatedWorkspaceSize;
    }

    protected void setEstimatedWorkspaceSize(long estimatedWorkspaceSize) {
        this.estimatedWorkspaceSize = estimatedWorkspaceSize;
    }
}
