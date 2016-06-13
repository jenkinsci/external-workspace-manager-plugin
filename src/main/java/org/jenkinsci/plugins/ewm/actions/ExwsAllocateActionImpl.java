package org.jenkinsci.plugins.ewm.actions;

import hudson.model.Run;
import jenkins.model.RunAction2;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.steps.model.ExternalWorkspace;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

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

    public List<ExternalWorkspace> getAllocatedWorkspaces() {
        return allocatedWorkspaces;
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

    @Override
    public void onAttached(Run<?, ?> run) {
        this.parent = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.parent = run;
    }
}
