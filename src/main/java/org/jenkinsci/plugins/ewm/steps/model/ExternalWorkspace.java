package org.jenkinsci.plugins.ewm.steps.model;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * POJO used to pass fields from one step to another.
 *
 * @author Alexandru Somai
 */
public final class ExternalWorkspace implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String diskId;
    private final String pathOnDisk;

    public ExternalWorkspace(@Nonnull String diskId, @Nonnull String pathOnDisk) {
        this.diskId = diskId;
        this.pathOnDisk = pathOnDisk;
    }

    @Nonnull
    public String getDiskId() {
        return diskId;
    }

    @Nonnull
    public String getPathOnDisk() {
        return pathOnDisk;
    }
}
