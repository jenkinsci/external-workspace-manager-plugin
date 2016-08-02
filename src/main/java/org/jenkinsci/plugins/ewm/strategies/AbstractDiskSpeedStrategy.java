package org.jenkinsci.plugins.ewm.strategies;

import hudson.AbortException;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.definitions.Disk;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
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
     * TODO temporary, to be discussed
     *
     * @param disks
     * @param estimatedWorkspaceSize
     * @return
     * @throws IOException
     */
    @Nonnull
    @Override
    public Disk allocateDisk(@Nonnull List<Disk> disks, @CheckForNull Long estimatedWorkspaceSize) throws IOException {
        if (estimatedWorkspaceSize == null) {
            // if the user didn't provide any estimatedWorkspaceSize, fallback to default method
            return allocateDisk(disks);
        }

        Iterator<Disk> iterator = disks.iterator();

        Disk candidate = null;
        // try to find at least one Disk candidate that has usable space > estimated workspace size
        while (iterator.hasNext()) {
            Disk next = iterator.next();
            String masterMountPoint = next.getMasterMountPoint();
            if (masterMountPoint == null) {
                String message = String.format("Mounting point from Master to the disk is not defined for Disk ID '%s'", next.getDiskId());
                throw new AbortException(message);
            }
            long usableSpace = new File(masterMountPoint).getUsableSpace();
            if (usableSpace > estimatedWorkspaceSize) {
                // we have found a possible candidate that has the usable space > estimate workspace size
                candidate = next;
                break;
            }
        }

        if (candidate == null) {
            // what if there is no disk that has usable space > estimated workspace size?
            // throw an exception maybe
            throw new AbortException("Couldn't find any Disk with the usable space at least " + estimatedWorkspaceSize);
        }

        Double candidateSpeed = getDiskSpeed(candidate.getDiskInfo());
        while (iterator.hasNext()) {
            Disk next = iterator.next();
            Double nextSpeed = getDiskSpeed(next.getDiskInfo());
            if (candidateSpeed != null && nextSpeed != null && nextSpeed > candidateSpeed) {
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
