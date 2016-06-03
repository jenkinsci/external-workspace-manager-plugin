package org.jenkinsci.plugins.ewm.actions;

import hudson.model.Action;
import org.jenkinsci.plugins.ewm.steps.model.ExternalWorkspace;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

/**
 * {@link Action} implementation for the {@link org.jenkinsci.plugins.ewm.steps.ExwsAllocateStep}.
 * Stores the allocated {@link ExternalWorkspace}.
 *
 * @author Alexandru Somai
 */
public class ExwsAllocateAction implements Action {

    private final transient FlowNode parent;
    private final ExternalWorkspace externalWorkspace;

    public ExwsAllocateAction(FlowNode parent, ExternalWorkspace externalWorkspace) {
        this.parent = parent;
        this.externalWorkspace = externalWorkspace;
    }

    public FlowNode getParent() {
        return parent;
    }

    public ExternalWorkspace getExternalWorkspace() {
        return externalWorkspace;
    }

    @Override
    public String getIconFileName() {
        return "none.png";
    }

    @Override
    public String getDisplayName() {
        return "External Workspace Allocate";
    }

    @Override
    public String getUrlName() {
        return "exwsAllocate";
    }
}
