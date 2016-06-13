package org.jenkinsci.plugins.ewm.steps.model;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.UUID;

/**
 * POJO used to pass fields from one step to another.
 *
 * @author Alexandru Somai
 */
public class ExternalWorkspace implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String diskPoolId;
    private final String diskId;
    private final String pathOnDisk;

    public ExternalWorkspace(@Nonnull String diskPoolId, @Nonnull String diskId, @Nonnull String pathOnDisk) {
        this.id = UUID.randomUUID();
        this.diskPoolId = diskPoolId;
        this.diskId = diskId;
        this.pathOnDisk = pathOnDisk;
    }

    @Nonnull
    public UUID getId() {
        return id;
    }

    @Nonnull
    public String getDiskPoolId() {
        return diskPoolId;
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
