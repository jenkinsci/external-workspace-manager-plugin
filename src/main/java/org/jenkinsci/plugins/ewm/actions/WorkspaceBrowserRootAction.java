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
import java.util.LinkedHashSet;
import java.util.Set;

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
     * found or the current user does not have both the {@link Item#READ} and {@link Item#WORKSPACE} permissions on any
     * of the jobs associated with the workspace
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

        // The workspace content may only be browsed by users that hold both the Item/Read and the Item/Workspace
        // permissions on at least one of the jobs associated with the fingerprint, i.e. the job that allocated the
        // workspace or any job that reused it. Item/Workspace is the dedicated permission that Jenkins core uses to
        // guard workspace browsing (see AbstractProject#doWs), because a workspace may contain secrets, and Item/Read is
        // required to reach the job in the first place; neither permission alone is sufficient. The same NoFingerprintMatch
        // response as for a missing fingerprint is returned on denial, so that the existence of the workspace is not
        // leaked.
        if (!hasWorkspacePermissionForAnyAssociatedJob(fingerprint)) {
            return new NoFingerprintMatch(workspaceId);
        }

        return facet.getWorkspace();
    }

    /**
     * Checks whether the current user holds both the {@link Item#READ} and {@link Item#WORKSPACE} permissions on any of
     * the jobs associated with the given fingerprint, i.e. the job that originally allocated the workspace or any job
     * that later reused it.
     *
     * @param fingerprint the fingerprint that holds the workspace metadata
     * @return {@code true} if the current user has {@link Item#READ} and {@link Item#WORKSPACE} on at least one
     * associated job, {@code false} otherwise
     */
    private static boolean hasWorkspacePermissionForAnyAssociatedJob(@Nonnull Fingerprint fingerprint) {
        Set<String> jobFullNames = new LinkedHashSet<>();
        Fingerprint.BuildPtr original = fingerprint.getOriginal();
        if (original != null) {
            jobFullNames.add(original.getName());
        }
        Hashtable<String, Fingerprint.RangeSet> usages = fingerprint.getUsages();
        if (usages != null) {
            jobFullNames.addAll(usages.keySet());
        }

        for (String jobFullName : jobFullNames) {
            if (hasWorkspacePermission(jobFullName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the current user may browse the workspace of the job with the given full name. This requires both
     * {@link Item#READ} and {@link Item#WORKSPACE}: {@link Jenkins#getItemByFullName(String, Class)} resolves the job
     * using the current user's authentication and returns {@code null} (or throws {@link AccessDeniedException}) when
     * the user lacks {@link Item#READ}, and the {@link Item#WORKSPACE} permission is then checked explicitly. This
     * matches how Jenkins core gates workspace browsing.
     *
     * @param jobFullName the full name of the job to resolve
     * @return {@code true} if the current user has {@link Item#READ} and {@link Item#WORKSPACE} on the job, {@code false}
     * otherwise
     */
    private static boolean hasWorkspacePermission(@Nonnull String jobFullName) {
        try {
            Job<?, ?> job = Jenkins.getActiveInstance().getItemByFullName(jobFullName, Job.class);
            return job != null && job.hasPermission(Item.WORKSPACE);
        } catch (AccessDeniedException e) {
            return false;
        }
    }
}
