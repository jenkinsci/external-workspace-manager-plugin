package org.jenkinsci.plugins.ewm.facets;

import hudson.model.Fingerprint;
import jenkins.model.FingerprintFacet;
import org.jenkinsci.plugins.ewm.model.ExternalWorkspace;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;

/**
 * {@link FingerprintFacet} implementation that holds the {@link ExternalWorkspace} metadata.
 *
 * @author Alexandru Somai
 */
public class WorkspaceBrowserFacet extends FingerprintFacet {

    private final ExternalWorkspace workspace;

    public WorkspaceBrowserFacet(@Nonnull Fingerprint fingerprint, long timestamp, @Nonnull ExternalWorkspace workspace) {
        super(fingerprint, timestamp);
        this.workspace = workspace;
    }

    @Restricted(NoExternalUse.class)
    @Nonnull
    public ExternalWorkspace getWorkspace() {
        return workspace;
    }
}
