package org.jenkinsci.plugins.ewm;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;

/**
 * @author Alexandru Somai
 */
@Restricted(NoExternalUse.class)
public abstract class DiskInfoProvider extends AbstractDescribableImpl<DiskInfoProvider> implements ExtensionPoint {

    @CheckForNull
    private String name;

    @CheckForNull
    private String type;

    @CheckForNull
    private Long size;

    @CheckForNull
    private Double writeSpeed;

    @CheckForNull
    private Double readSpeed;

    @CheckForNull
    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    @CheckForNull
    public String getType() {
        return type;
    }

    protected void setType(String type) {
        this.type = type;
    }

    @CheckForNull
    public Long getSize() {
        return size;
    }

    protected void setSize(Long size) {
        this.size = size;
    }

    @CheckForNull
    public Double getWriteSpeed() {
        return writeSpeed;
    }

    protected void setWriteSpeed(Double writeSpeed) {
        this.writeSpeed = writeSpeed;
    }

    @CheckForNull
    public Double getReadSpeed() {
        return readSpeed;
    }

    protected void setReadSpeed(Double readSpeed) {
        this.readSpeed = readSpeed;
    }
}
