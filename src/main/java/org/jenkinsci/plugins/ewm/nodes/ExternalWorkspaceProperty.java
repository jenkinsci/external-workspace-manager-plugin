package org.jenkinsci.plugins.ewm.nodes;

import hudson.Extension;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

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

    @DataBoundConstructor
    public ExternalWorkspaceProperty(List<NodeDiskPool> nodeDiskPools) {
        this.getDescriptor().setDiskPools(fixNull(nodeDiskPools));
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Nonnull
    public List<NodeDiskPool> getNodeDiskPools() {
        return getDescriptor().getNodeDiskPools();
    }

    @Extension
    @Symbol("exwsNodeConfigurationDiskPools")
    public static class DescriptorImpl extends NodePropertyDescriptor {

        private List<NodeDiskPool> nodeDiskPools = Collections.emptyList();

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.nodes_ExternalWorkspaceProperty_DisplayName();
        }

        @Nonnull
        public List<NodeDiskPool> getNodeDiskPools() {
            return Collections.unmodifiableList(nodeDiskPools);
        }

        @DataBoundSetter
        public void setDiskPools(List<NodeDiskPool> nodeDiskPools) {
            this.nodeDiskPools = nodeDiskPools;
        }
    }
}
