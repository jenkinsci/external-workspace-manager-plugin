package org.jenkinsci.plugins.ewm.actions;

import hudson.Extension;
import hudson.model.Fingerprint;
import hudson.model.NoFingerprintMatch;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.facets.WorkspaceBrowserFacet;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.io.IOException;

/**
 * TODO JAVADOC
 *
 * @author Alexandru Somai
 */
@Extension
public class WorkspaceBrowserRootAction implements RootAction {

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return Messages.actions_WorkspaceBrowserRootAction_DisplayName();
    }

    @Override
    public String getUrlName() {
        return "exws";
    }

    /**
     * TODO JAVADOC
     *
     * @param token
     * @return
     * @throws IOException
     */
    @Restricted(NoExternalUse.class)
    @SuppressWarnings("unused")
    public Object getBrowse(String token) throws IOException {
        Fingerprint f = Jenkins.getActiveInstance()._getFingerprint(token);
        if (f == null) {
            return new NoFingerprintMatch(token);
        }

        return WorkspaceBrowserFacet.getWorkspace(f);
    }
}
