package org.jenkinsci.plugins.ewm.model;

import org.jenkinsci.plugins.ewm.utils.RandomUtil;

import hudson.FilePath;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.ModelObject;
import org.jenkinsci.plugins.ewm.utils.Util;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.File;
import java.io.Serializable;

/**
 * POJO used to pass fields from one step to another.
 *
 * @author Alexandru Somai
 */
public class ExternalWorkspace implements Serializable, ModelObject {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String diskPoolId;
    private final String diskId;
    private final String masterMountPoint;
    private final String pathOnDisk;
    private String workspaceUrl;

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

    public void setWorkspaceUrl(@Nonnull String workspaceUrl) {
        this.workspaceUrl = workspaceUrl;
    }

    @Nonnull
    public String getWorkspaceUrl() {
        return workspaceUrl;
    }

    @Override
    public String getDisplayName() {
        return String.format("External Workspace on Disk ID: %s from Disk Pool ID: %s", diskId, diskPoolId);
    }

    @Restricted(NoExternalUse.class)
    // TODO how to reference this from index.jelly file?
    public DirectoryBrowserSupport doWs(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        FilePath ws = new FilePath(new File(masterMountPoint, pathOnDisk));
        if (!ws.exists()) {
            req.getView(this, "noWorkspace.jelly").forward(req, rsp);
            return null;
        } else {
            return new DirectoryBrowserSupport(this, ws, getDisplayName(), "folder.png", true);
        }
    }
}
