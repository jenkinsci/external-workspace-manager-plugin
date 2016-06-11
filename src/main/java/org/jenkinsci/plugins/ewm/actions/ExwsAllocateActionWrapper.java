package org.jenkinsci.plugins.ewm.actions;

import hudson.model.Actionable;
import hudson.model.Run;
import jenkins.model.RunAction2;
import org.jenkinsci.plugins.ewm.Messages;

/**
 * A {@link RunAction2} that acts as a wrapper to hold multiple {@link ExwsAllocateActionImpl}s.
 *
 * @author Alexandru Somai
 */
public class ExwsAllocateActionWrapper extends Actionable implements RunAction2 {

    private Run parent;

    public Run getParent() {
        return parent;
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
    public String getSearchUrl() {
        return getUrlName();
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
