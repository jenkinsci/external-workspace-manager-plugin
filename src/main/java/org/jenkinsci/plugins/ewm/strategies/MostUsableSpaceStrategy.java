package org.jenkinsci.plugins.ewm.strategies;

import hudson.AbortException;
import hudson.Extension;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategyDescriptor;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * {@link DiskAllocationStrategy} implementation that allocates the disk with the most usable space.
 *
 * @author Alexandru Somai
 */
public class MostUsableSpaceStrategy extends DiskAllocationStrategy {

    @DataBoundConstructor
    public MostUsableSpaceStrategy() {
    }

    @DataBoundSetter
    public void setEstimatedWorkspaceSize(@Nonnull Long estimatedWorkspaceSize) {
        if (estimatedWorkspaceSize == 0) {
            estimatedWorkspaceSize = null;
        }
        super.setEstimatedWorkspaceSize(estimatedWorkspaceSize);
    }

    @Nonnull
    @Override
    public Disk allocateDisk(@Nonnull List<Disk> disks) throws IOException {
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
     * @throws IOException if mounting point from Jenkins Master to Disk is {@code null}
     */
    private long retrieveUsableSpace(Disk disk) throws IOException {
        String masterMountPoint = disk.getMasterMountPoint();
        if (masterMountPoint == null) {
            String message = String.format("Mounting point from Master to the disk is not defined for Disk ID '%s'", disk.getDiskId());
            throw new AbortException(message);
        }

        return new File(masterMountPoint).getUsableSpace();
    }

    @Extension(ordinal = 1000)
    public static class DescriptorImpl extends DiskAllocationStrategyDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.strategies_MostUsableSpace_DisplayName();
        }
    }
}
