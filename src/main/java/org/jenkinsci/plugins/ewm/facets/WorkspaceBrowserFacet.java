package org.jenkinsci.plugins.ewm.facets;

import hudson.model.Fingerprint;
import jenkins.model.FingerprintFacet;
import org.jenkinsci.plugins.ewm.model.ExternalWorkspace;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

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
     * @param fingerprint
     * @return
     */
    @Restricted(NoExternalUse.class)
    @Nonnull
    public static ExternalWorkspace getWorkspace(Fingerprint fingerprint) {
        DiskStatsInfoFacet stats = fingerprint.getFacet(DiskStatsInfoFacet.class);
        if (stats == null) {
            throw new IllegalArgumentException("Couldn't find the Fingerprint Facet that holds the Workspace metadata");
        }

        return stats.getWorkspace();
    }

    /**
     * TODO JAVADOC
     *
     * @return
     */
    @Restricted(NoExternalUse.class)
    @Nonnull
    public ExternalWorkspace getWorkspace() {
        return getWorkspace(getFingerprint());
    }
}
