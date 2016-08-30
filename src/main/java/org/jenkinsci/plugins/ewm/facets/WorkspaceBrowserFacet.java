package org.jenkinsci.plugins.ewm.facets;

import hudson.model.Fingerprint;
import jenkins.model.FingerprintFacet;
import org.jenkinsci.plugins.ewm.model.ExternalWorkspace;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * TODO JAVADOC
 *
 * @author Alexandru Somai
 */
public class WorkspaceBrowserFacet extends FingerprintFacet {

    public WorkspaceBrowserFacet(@Nonnull Fingerprint fingerprint, long timestamp) {
        super(fingerprint, timestamp);
    }

    /**
     * TODO JAVADOC
     *
     * @return
     */
    @Restricted(NoExternalUse.class)
    @SuppressWarnings("unused")
    public String getWorkspaceLink() {
        ExternalWorkspace workspace = getWorkspace();
        if (workspace == null) {
            return "#";
        }

        Fingerprint.BuildPtr original = getFingerprint().getOriginal();
        if (original == null) {
            return "#";
        }

        return original.getJob().getAbsoluteUrl() + original.getRun().getSearchUrl() +
                "exwsAllocate/" + workspace.getId() + "/ws/";
    }

    @CheckForNull
    private ExternalWorkspace getWorkspace() {
        DiskStatsInfoFacet stats = getFingerprint().getFacet(DiskStatsInfoFacet.class);
        if (stats == null) {
            return null;
        }

        return stats.getWorkspace();
    }
}
