package org.jenkinsci.plugins.ewm.nodes;

import hudson.Extension;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static hudson.Util.fixNull;

/**
 * A {@link NodeProperty} where are defined the {@link NodeDiskPool} definitions.
 *
 * @author Alexandru Somai
 */
public class ExternalWorkspaceProperty extends NodeProperty<Node> {

    private final List<NodeDiskPool> nodeDiskPools;

    @DataBoundConstructor
    public ExternalWorkspaceProperty(List<NodeDiskPool> nodeDiskPools) {
        this.nodeDiskPools = fixNull(nodeDiskPools);
    }

    @Nonnull
    public List<NodeDiskPool> getNodeDiskPools() {
        return Collections.unmodifiableList(nodeDiskPools);
    }

    @Extension
    @Symbol("exwsNodeConfigurationDiskPools")
    public static class DescriptorImpl extends NodePropertyDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.nodes_ExternalWorkspaceProperty_DisplayName();
        }
    }
}
