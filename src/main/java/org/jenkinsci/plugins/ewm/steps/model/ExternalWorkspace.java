package org.jenkinsci.plugins.ewm.steps.model;

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

    public ExternalWorkspace(String diskId, String pathOnDisk) {
        this.diskId = diskId;
        this.pathOnDisk = pathOnDisk;
    }

    public String getDiskId() {
        return diskId;
    }

    public String getPathOnDisk() {
        return pathOnDisk;
    }
}
