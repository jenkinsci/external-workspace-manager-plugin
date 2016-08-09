package org.jenkinsci.plugins.ewm;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;

/**
 * Contains additional information about a {@link org.jenkinsci.plugins.ewm.definitions.Disk} entry.
 *
 * @author Alexandru Somai
 */
@Restricted(NoExternalUse.class)
public abstract class DiskInfoProvider extends AbstractDescribableImpl<DiskInfoProvider> implements ExtensionPoint {

    private final int readSpeed;
    private final int writeSpeed;

    protected DiskInfoProvider() {
        this(0, 0);
    }

    protected DiskInfoProvider(int readSpeed, int writeSpeed) {
        this.readSpeed = readSpeed > 0 ? readSpeed : 0;
        this.writeSpeed = writeSpeed > 0 ? writeSpeed : 0;
    }

    /**
     * @return all registered {@link DiskInfoProvider}s.
     */
    @Nonnull
    public static ExtensionList<DiskInfoProvider> all() {
        return ExtensionList.lookup(DiskInfoProvider.class);
    }

    /**
     * @return the registered {@link DiskInfoProviderDescriptor}s for the {@link DiskInfoProvider}.
     */
    @Nonnull
    public static ExtensionList<DiskInfoProviderDescriptor> allDescriptors() {
        return ExtensionList.lookup(DiskInfoProviderDescriptor.class);
    }

    public int getReadSpeed() {
        return readSpeed;
    }

    public int getWriteSpeed() {
        return writeSpeed;
    }
}
