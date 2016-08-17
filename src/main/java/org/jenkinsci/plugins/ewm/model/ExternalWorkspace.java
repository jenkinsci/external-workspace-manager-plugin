package org.jenkinsci.plugins.ewm.model;

import org.jenkinsci.plugins.ewm.utils.RandomUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Serializable;

/**
 * POJO used to pass fields from one step to another.
 *
 * @author Alexandru Somai
 */
public class ExternalWorkspace implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String diskPoolId;
    private final String diskId;
    private final String masterMountPoint;
    private final String pathOnDisk;

    public ExternalWorkspace(@Nonnull String diskPoolId, @Nonnull String diskId,
                             @Nonnull String masterMountPoint, @Nonnull String pathOnDisk) {
        this.id = RandomUtil.generateRandomHexString(32);
        this.diskPoolId = diskPoolId;
        this.diskId = diskId;
        this.masterMountPoint = masterMountPoint;
        this.pathOnDisk = pathOnDisk;
    }

    @Nonnull
    public String getId() {
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
    public String getMasterMountPoint() {
        return masterMountPoint;
    }

    @Nonnull
    public String getPathOnDisk() {
        return pathOnDisk;
    }

    /**
     * Computes the complete workspace path, by appending {@link #pathOnDisk} to the {@link #masterMountPoint}.
     * It's recommended to use this method when the complete workspace path is needed, instead of manually appending
     * the two strings, with separator between.
     *
     * @return the complete workspace path from Jenkins master
     */
    @Nonnull
    @SuppressWarnings("unused")
    public String getCompleteWorkspacePath() {
        return new File(masterMountPoint, pathOnDisk).getPath();
    }
}
