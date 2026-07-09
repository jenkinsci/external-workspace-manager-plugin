package org.jenkinsci.plugins.ewm.actions;

import hudson.Extension;
import hudson.model.Fingerprint;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.NoFingerprintMatch;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.facets.WorkspaceBrowserFacet;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Hashtable;

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
     * @return the workspace whose id matches the given input id, or {@link NoFingerprintMatch} if the fingerprint is not
     * found or the current user is not allowed to read any of the jobs associated with the workspace
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

        // The workspace content may only be browsed by users that are allowed to read at least one of the jobs
        // associated with the fingerprint, i.e. the job that allocated the workspace or any job that reused it.
        // Otherwise a user holding only Overall/Read could read another job's workspace files just by knowing the
        // workspace id. The same NoFingerprintMatch response as for a missing fingerprint is returned on denial, so
        // that the existence of the workspace is not leaked.
        if (!hasReadPermissionForAnyAssociatedJob(fingerprint)) {
            return new NoFingerprintMatch(workspaceId);
        }

        return facet.getWorkspace();
    }

    /**
     * Checks whether the current user is allowed to read any of the jobs associated with the given fingerprint, i.e.
     * the job that originally allocated the workspace or any job that later reused it.
     *
     * @param fingerprint the fingerprint that holds the workspace metadata
     * @return {@code true} if the current user can read at least one associated job, {@code false} otherwise
     */
    private static boolean hasReadPermissionForAnyAssociatedJob(@Nonnull Fingerprint fingerprint) {
        Fingerprint.BuildPtr original = fingerprint.getOriginal();
        if (original != null && canReadJob(original.getName())) {
            return true;
        }

        Hashtable<String, Fingerprint.RangeSet> usages = fingerprint.getUsages();
        if (usages != null) {
            for (String jobFullName : usages.keySet()) {
                if (canReadJob(jobFullName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Resolves a job by its full name using the current user's authentication. {@link Jenkins#getItemByFullName(String,
     * Class)} returns {@code null} (or throws {@link AccessDeniedException}) when the user lacks {@link Item#READ} on the
     * resolved item, which is exactly the permission that must be enforced before serving workspace content.
     *
     * @param jobFullName the full name of the job to resolve
     * @return {@code true} if the current user can read the job, {@code false} otherwise
     */
    private static boolean canReadJob(@Nonnull String jobFullName) {
        try {
            return Jenkins.getActiveInstance().getItemByFullName(jobFullName, Job.class) != null;
        } catch (AccessDeniedException e) {
            return false;
        }
    }
}
