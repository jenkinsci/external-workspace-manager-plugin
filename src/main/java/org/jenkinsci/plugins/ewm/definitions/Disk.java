package org.jenkinsci.plugins.ewm.definitions;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.DiskInfoProviderDescriptor;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.providers.NoDiskInfo;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.Util.isRelativePath;
import static hudson.util.FormValidation.validateRequired;

/**
 * Describable used to define the disk information within a {@link DiskPool}.
 *
 * @author Alexandru Somai
 */
public abstract class Disk extends AbstractDescribableImpl<Disk> {

    private final String diskId;
    private final String displayName;
    private final String masterMountPoint;
    private final String physicalPathOnDisk;
    private final DiskInfoProvider diskInfo;


    // TODO : should add a default constructor here ?
    protected Disk(String diskId, String displayName, String masterMountPoint,
                String physicalPathOnDisk, DiskInfoProvider diskInfo) {
        this.diskId = fixEmptyAndTrim(diskId);
        this.displayName = fixEmptyAndTrim(displayName);
        this.masterMountPoint = fixEmptyAndTrim(masterMountPoint);
        this.physicalPathOnDisk = fixEmptyAndTrim(physicalPathOnDisk);
        this.diskInfo = diskInfo == null ? new NoDiskInfo() : diskInfo;
    }

    // TODO : need a descriptor all() method, to get all its subclass descriptor
    // TODO : but why do I need this ?
    /**
     * @return all registered {@link Disk}s.
     */
    @Nonnull
    public static ExtensionList<Disk> all() {
        return ExtensionList.lookup(Disk.class);
    }

    /**
     * @return the registered {@link Disk}s for the {@link Disk}.
     */
    @Nonnull
    public static ExtensionList<Disk> allDescriptors() {
        return ExtensionList.lookup(Disk.class);
    }

    @CheckForNull
    public String getDiskId() {
        return diskId;
    }

    @CheckForNull
    public String getDisplayName() {
        return displayName != null ? displayName : diskId;
    }

    @CheckForNull
    public String getMasterMountPoint() {
        return masterMountPoint;
    }

    @CheckForNull
    public String getPhysicalPathOnDisk() {
        return physicalPathOnDisk;
    }

    @Nonnull
    public DiskInfoProvider getDiskInfo() {
        return diskInfo;
    }
}

