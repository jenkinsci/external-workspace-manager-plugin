package org.jenkinsci.plugins.ewm;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import org.jenkinsci.plugins.ewm.definitions.Disk;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Abstract class for defining a disk allocation strategy.
 * Each disk allocation strategy should extend this class and provide its own implementation of disk allocation.
 *
 * @author Alexandru Somai
 */
public abstract class DiskAllocationStrategy extends AbstractDescribableImpl<DiskAllocationStrategy> implements ExtensionPoint {

    @CheckForNull
    private Long estimatedWorkspaceSize;

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
     * TODO add javadoc if we choose this solution
     *
     * @param disks
     * @param estimatedWorkspaceSize
     * @return
     * @throws IOException
     */
    @Nonnull
    public Disk allocateDisk(@Nonnull List<Disk> disks, @CheckForNull Long estimatedWorkspaceSize) throws IOException {
        return allocateDisk(disks);
    }

    @CheckForNull
    public Long getEstimatedWorkspaceSize() {
        return estimatedWorkspaceSize;
    }

    protected void setEstimatedWorkspaceSize(Long estimatedWorkspaceSize) {
        this.estimatedWorkspaceSize = estimatedWorkspaceSize;
    }
}
