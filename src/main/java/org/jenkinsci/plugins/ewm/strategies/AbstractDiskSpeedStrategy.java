package org.jenkinsci.plugins.ewm.strategies;

import hudson.AbortException;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.definitions.Disk;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Iterator;
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
        long estimatedWorkspaceSize = getEstimatedWorkspaceSize();

        Iterator<Disk> iterator = disks.iterator();
        Disk candidate = null;

        // try to find at least one Disk candidate that has usable space > estimated workspace size
        while (iterator.hasNext()) {
            Disk next = iterator.next();
            long usableSpace = retrieveUsableSpace(next);
            if (usableSpace > estimatedWorkspaceSize) {
                // found a possible candidate that has the usable space > estimate workspace size
                candidate = next;
                break;
            }
        }

        if (candidate == null) {
            // there is no Disk that has usable space > estimated workspace size
            String message = String.format("Couldn't find any Disk with at least %s KB usable space", estimatedWorkspaceSize);
            throw new AbortException(message);
        }

        // found a possible candidate, continue searching for another disk with speed > candidate's speed
        Double candidateSpeed = getDiskSpeed(candidate.getDiskInfo());
        while (iterator.hasNext()) {
            Disk next = iterator.next();
            Double nextSpeed = getDiskSpeed(next.getDiskInfo());
            long usableSpace = retrieveUsableSpace(next);
            if (candidateSpeed != null && nextSpeed != null && nextSpeed > candidateSpeed && usableSpace > estimatedWorkspaceSize) {
                // found another Disk that has higher speed than the candidate's speed
                // and the usable space > estimated workspace size
                candidate = next;
                candidateSpeed = nextSpeed;
            }
        }

        return candidate;
    }

    /**
     * Override this method to return the speed property on which the selection should be made.
     *
     * @param diskInfo the {@link DiskInfoProvider} that contains R/W {@link Disk} speed
     * @return the value represented by the R/W speed
     */
    @CheckForNull
    protected abstract Double getDiskSpeed(@Nonnull DiskInfoProvider diskInfo);

    @Override
    protected void setEstimatedWorkspaceSize(@Nonnull Long estimatedWorkspaceSize) {
        if (estimatedWorkspaceSize == 0) {
            estimatedWorkspaceSize = null;
        }
        super.setEstimatedWorkspaceSize(estimatedWorkspaceSize);
    }
}
