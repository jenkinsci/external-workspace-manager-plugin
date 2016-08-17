package org.jenkinsci.plugins.ewm.actions;

import hudson.FilePath;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.labels.LabelAtom;
import hudson.security.AccessControlled;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.steps.model.ExternalWorkspace;
import org.jenkinsci.plugins.workflow.actions.FlowNodeAction;
import org.jenkinsci.plugins.workflow.actions.WorkspaceAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

/**
 * @author Alexandru Somai
 */
public class ExternalWorkspaceActionImpl extends WorkspaceAction implements FlowNodeAction {

    private final String node;
    private final String path;
    private final Set<LabelAtom> labels;
    private transient FlowNode parent;

    public ExternalWorkspaceActionImpl(ExternalWorkspace externalWorkspace, FlowNode parent) {
        Jenkins masterNode = Jenkins.getActiveInstance();
        this.node = masterNode.getFullName();
        this.labels = masterNode.getLabelAtoms();
        this.path = new File(externalWorkspace.getMasterMountPoint(), externalWorkspace.getPathOnDisk()).getPath();
        this.parent = parent;
    }

    @Nonnull
    @Override
    public String getNode() {
        return node;
    }

    @Nonnull
    @Override
    public String getPath() {
        return path;
    }

    @Nonnull
    @Override
    public Set<LabelAtom> getLabels() {
        return labels;
    }

    @Override
    public String getIconFileName() {
        return "folder.png";
    }

    @Override
    public String getDisplayName() {
        return "External Workspace";
    }

    @Override
    public String getUrlName() {
        return "ws";
    }

    @Override
    public void onLoad(FlowNode parent) {
        this.parent = parent;
    }

    @Restricted(NoExternalUse.class)
    public FlowNode getParent() {
        return parent;
    }

    public DirectoryBrowserSupport doDynamic() throws IOException {
        Queue.Executable executable = parent.getExecution().getOwner().getExecutable();
        if (executable instanceof AccessControlled) {
            ((AccessControlled) executable).checkPermission(Item.WORKSPACE);
        }
        FilePath ws = getWorkspace();
        if (ws == null) {
            throw new FileNotFoundException();
        }
        return new DirectoryBrowserSupport(this, ws, getDisplayName(), getIconFileName(), true);
    }
}
