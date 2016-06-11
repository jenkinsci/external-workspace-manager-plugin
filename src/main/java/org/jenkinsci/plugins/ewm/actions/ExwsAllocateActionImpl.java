package org.jenkinsci.plugins.ewm.actions;

import hudson.model.Action;
import hudson.model.InvisibleAction;
import org.jenkinsci.plugins.ewm.steps.model.ExternalWorkspace;

/**
 * {@link Action} implementation for the {@link org.jenkinsci.plugins.ewm.steps.ExwsAllocateStep}.
 * Stores the allocated {@link ExternalWorkspace} that will be later used by the downstream job.
 *
 * @author Alexandru Somai
 */
public class ExwsAllocateActionImpl extends InvisibleAction {

    private final ExternalWorkspace externalWorkspace;

    public ExwsAllocateActionImpl(ExternalWorkspace externalWorkspace) {
        this.externalWorkspace = externalWorkspace;
    }

    public ExternalWorkspace getExternalWorkspace() {
        return externalWorkspace;
    }
}
