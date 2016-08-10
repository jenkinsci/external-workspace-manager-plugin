package org.jenkinsci.plugins.ewm;

import hudson.model.Descriptor;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Descriptor for {@link DiskInfoProvider}.
 *
 * @author Alexandru Somai
 */
@Restricted(NoExternalUse.class)
public abstract class DiskInfoProviderDescriptor extends Descriptor<DiskInfoProvider> {

    public DiskInfoProviderDescriptor() {
        super();
    }

    /**
     * @param clazz class to describe
     * @see Descriptor#Descriptor(Class)
     */
    public DiskInfoProviderDescriptor(Class<? extends DiskInfoProvider> clazz) {
        super(clazz);
    }
}
