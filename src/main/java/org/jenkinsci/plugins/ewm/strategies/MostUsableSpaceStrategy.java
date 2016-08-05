package org.jenkinsci.plugins.ewm.strategies;

import hudson.AbortException;
import hudson.Extension;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategyDescriptor;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * {@link DiskAllocationStrategy} implementation that allocates the disk with the most usable space.
 *
 * @author Alexandru Somai
 */
@Extension
public class MostUsableSpaceStrategy extends DiskAllocationStrategy {

    @DataBoundConstructor
    public MostUsableSpaceStrategy() {
    }

    @DataBoundSetter
    public void setEstimatedWorkspaceSize(long estimatedWorkspaceSize) {
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

        if (selectedDiskUsableSpace < getEstimatedWorkspaceSizeInKilobytes()) {
            String message = String.format("The selected Disk with the most usable space doesn't have at least %s MB space", getEstimatedWorkspaceSize());
            throw new AbortException(message);
        }

        return selectedDisk;
    }

    @Symbol("mostUsableSpace")
    @Extension(ordinal = 1000)
    public static class DescriptorImpl extends DiskAllocationStrategyDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.strategies_MostUsableSpace_DisplayName();
        }
    }
}
