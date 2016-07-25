package org.jenkinsci.plugins.ewm;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;

import static hudson.Util.fixEmptyAndTrim;

/**
 * @author Alexandru Somai
 */
@Restricted(NoExternalUse.class)
public abstract class DiskInfoProvider extends AbstractDescribableImpl<DiskInfoProvider> implements ExtensionPoint {

    // TODO - do we need name? see {@link Disk#displayName}
    @CheckForNull
    private final String name;

    @CheckForNull
    private final String type;

    @CheckForNull
    private final Long size;

    @CheckForNull
    private final Double writeSpeed;

    @CheckForNull
    private final Double readSpeed;

    protected DiskInfoProvider(@CheckForNull String name, @CheckForNull String type, @CheckForNull Long size,
                               @CheckForNull Double writeSpeed, @CheckForNull Double readSpeed) {
        this.name = fixEmptyAndTrim(name);
        this.type = fixEmptyAndTrim(type);
        this.size = size != null && size >= 0 ? size : null;
        this.writeSpeed = writeSpeed != null && writeSpeed >= 0 ? writeSpeed : null;
        this.readSpeed = readSpeed != null && readSpeed >= 0 ? readSpeed : null;
    }

    /**
     * All registered {@link DiskInfoProvider}s.
     */
    public static ExtensionList<DiskInfoProvider> all() {
        return Jenkins.getActiveInstance().getExtensionList(DiskInfoProvider.class);
    }

    @CheckForNull
    public String getName() {
        return name;
    }

    @CheckForNull
    public String getType() {
        return type;
    }

    @CheckForNull
    public Long getSize() {
        return size;
    }

    @CheckForNull
    public Double getWriteSpeed() {
        return writeSpeed;
    }

    @CheckForNull
    public Double getReadSpeed() {
        return readSpeed;
    }
}
