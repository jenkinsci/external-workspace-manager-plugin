package org.jenkinsci.plugins.ewm.actions;

import hudson.FilePath;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Run;
import jenkins.model.RunAction2;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.model.ExternalWorkspace;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link RunAction2} implementation for the {@link org.jenkinsci.plugins.ewm.steps.ExwsAllocateStep}.
 * Stores the allocated {@link ExternalWorkspace}(s) that will be later used by the downstream job.
 *
 * @author Alexandru Somai
 */
public class ExwsAllocateActionImpl implements RunAction2 {

    private final List<ExternalWorkspace> allocatedWorkspaces = new LinkedList<>();
    private Run parent;

    @Restricted(NoExternalUse.class)
    public Run getParent() {
        return parent;
    }

    public void addAllocatedWorkspace(ExternalWorkspace externalWorkspace) {
        allocatedWorkspaces.add(externalWorkspace);
    }

    @Nonnull
    public List<ExternalWorkspace> getAllocatedWorkspaces() {
        return Collections.unmodifiableList(allocatedWorkspaces);
    }

    @Override
    public String getIconFileName() {
        // TODO change with an appropriate icon file
        return "folder.png";
    }

    @Override
    public String getDisplayName() {
        return Messages.actions_ExwsAllocateAction_DisplayName();
    }

    @Override
    public String getUrlName() {
        return "exwsAllocate";
    }

    @Restricted(NoExternalUse.class)
    public DirectoryBrowserSupport doWs(StaplerRequest req, StaplerResponse rsp, @QueryParameter String id) throws IOException, ServletException, InterruptedException {
        ExternalWorkspace exws = null;

        // TODO makes sense to make #allocatedWorkspaces a HashMap
        for (ExternalWorkspace allocatedWorkspace : allocatedWorkspaces) {
            if (allocatedWorkspace.getId().equals(id)) {
                exws = allocatedWorkspace;
                break;
            }
        }

        if (exws == null) {
            return null;
        }

        FilePath ws = new FilePath(new File(exws.getMasterMountPoint(), exws.getPathOnDisk()));
        if (!ws.exists()) {
            req.getView(this, "noWorkspace.jelly").forward(req, rsp);
            return null;
        } else {
            return new DirectoryBrowserSupport(exws, ws, exws.getDisplayName(), "folder.png", true);
        }
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.parent = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.parent = run;
    }
}
