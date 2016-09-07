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

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * {@link RootAction} implementation that handles workspace browsing by its id.
 *
 * @author Alexandru Somai
 */
@Extension
@SuppressWarnings("unused")
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
     * Method accessed by the Stapler framework when the following url is accessed:
     * <i>JENKINS_ROOT_URL/exws/browse/workspaceId/</i>
     *
     * @param workspaceId the workspace's unique id
     * @return the workspace whose id matches the given input id, or {@link NoFingerprintMatch} if fingerprint is not found
     * @throws IOException              if fingerprint load operation fails
     * @throws IllegalArgumentException if {@link WorkspaceBrowserFacet} is not registered for the matching fingerprint
     */
    @Restricted(NoExternalUse.class)
    @SuppressWarnings("unused")
    @Nonnull
    public Object getBrowse(String workspaceId) throws IOException {
        Fingerprint fingerprint = Jenkins.getActiveInstance()._getFingerprint(workspaceId);
        if (fingerprint == null) {
            return new NoFingerprintMatch(workspaceId);
        }

        WorkspaceBrowserFacet facet = fingerprint.getFacet(WorkspaceBrowserFacet.class);
        if (facet == null) {
            throw new IllegalArgumentException("Couldn't find the Fingerprint Facet that holds the Workspace metadata");
        }

        return facet.getWorkspace();
    }
}
