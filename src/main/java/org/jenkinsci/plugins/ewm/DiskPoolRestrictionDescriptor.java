package org.jenkinsci.plugins.ewm;

import hudson.model.Descriptor;

/**
 * Descriptor for {@link DiskPoolRestriction}.
 */
public abstract class DiskPoolRestrictionDescriptor extends Descriptor<DiskPoolRestriction> {

    public DiskPoolRestrictionDescriptor() {
        super();
    }

    /**
     * @param clazz class to describe
     * @see Descriptor#Descriptor(Class)
     */
    public DiskPoolRestrictionDescriptor(Class<? extends DiskPoolRestriction> clazz) {
        super(clazz);
    }
}
