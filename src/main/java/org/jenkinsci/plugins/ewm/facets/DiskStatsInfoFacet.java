package org.jenkinsci.plugins.ewm.facets;

import hudson.model.Fingerprint;
import jenkins.model.FingerprintFacet;
import org.jenkinsci.plugins.ewm.model.ExternalWorkspace;

import javax.annotation.Nonnull;

/**
 * TODO JAVADOC
 *
 * @author Alexandru Somai
 */
public class DiskStatsInfoFacet extends FingerprintFacet {

    private final ExternalWorkspace workspace;

    public DiskStatsInfoFacet(@Nonnull Fingerprint fingerprint, long timestamp, @Nonnull ExternalWorkspace workspace) {
        super(fingerprint, timestamp);
        this.workspace = workspace;
    }

    @Nonnull
    public ExternalWorkspace getWorkspace() {
        return workspace;
    }
}
