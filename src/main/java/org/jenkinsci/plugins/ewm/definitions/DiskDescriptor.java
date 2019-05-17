package org.jenkinsci.plugins.ewm.definitions;

import hudson.model.Descriptor;

public abstract class DiskDescriptor extends Descriptor<Disk> {

    public DiskDescriptor() {
        super();
    }

    /**
     * @param clazz class to describe
     * @see Descriptor#Descriptor(Class)
     */
    // TODO : what does this line mean
    public DiskDescriptor(Class<? extends Disk> clazz) {
        super(clazz);
    }
}
