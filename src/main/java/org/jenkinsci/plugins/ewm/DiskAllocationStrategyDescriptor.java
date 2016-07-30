package org.jenkinsci.plugins.ewm;

import hudson.model.Descriptor;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Descriptor for {@link DiskAllocationStrategy}.
 *
 * @author Alexandru Somai
 */
@Restricted(NoExternalUse.class)
public abstract class DiskAllocationStrategyDescriptor extends Descriptor<DiskAllocationStrategy> {

    public DiskAllocationStrategyDescriptor() {
        super();
    }

    /**
     * @param clazz class to describe
     * @see Descriptor#Descriptor(Class)
     */
    public DiskAllocationStrategyDescriptor(Class<? extends DiskAllocationStrategy> clazz) {
        super(clazz);
    }
}
