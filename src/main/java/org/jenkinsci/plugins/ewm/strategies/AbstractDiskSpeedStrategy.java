package org.jenkinsci.plugins.ewm.strategies;

import hudson.AbortException;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.definitions.Disk;

import javax.annotation.Nonnull;
import java.io.IOException;
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
        long estimatedWorkspaceSizeInBytes = getEstimatedWorkspaceSizeInBytes();
        Disk candidate = null;

        for (Disk disk : disks) {
            long usableSpace = retrieveUsableSpace(disk);

            if (candidate == null && usableSpace >= estimatedWorkspaceSizeInBytes) {
                // found a possible candidate that has the usable space >= estimated workspace size
                candidate = disk;
            }

            if (candidate != null) {
                // possible candidate, continue searching for another disk with speed > candidate's speed
                int candidateSpeed = getDiskSpeed(candidate.getDiskInfo());
                int diskSpeed = getDiskSpeed(disk.getDiskInfo());

                if (diskSpeed > candidateSpeed && usableSpace >= estimatedWorkspaceSizeInBytes) {
                    // found another disk that has higher speed than the candidate's speed and the usable space >= estimated workspace size
                    candidate = disk;
                }
            }
        }

        if (candidate == null) {
            // there is no disk that has usable space >= estimated workspace size
            String message = String.format("Couldn't find any Disk with at least %s MB usable space", getEstimatedWorkspaceSize());
            throw new AbortException(message);
        }

        return candidate;
    }

    /**
     * Override this method to return the speed property on which the selection should be made.
     *
     * @param diskInfo the {@link DiskInfoProvider} that contains R/W {@link Disk} speed
     * @return the value represented by the R/W speed
     */
    protected abstract int getDiskSpeed(@Nonnull DiskInfoProvider diskInfo);
}
