package org.jenkinsci.plugins.ewm.nodes;

import hudson.Extension;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;

import static hudson.Util.fixNull;

/**
 * A {@link NodeProperty} where are defined the {@link DiskPoolNode} definitions.
 *
 * @author Alexandru Somai
 */
public class ExternalWorkspaceProperty extends NodeProperty<Node> {

    private final List<DiskPoolNode> diskPoolNodes;

    @DataBoundConstructor
    public ExternalWorkspaceProperty(List<DiskPoolNode> diskPoolNodes) {
        this.diskPoolNodes = fixNull(diskPoolNodes);
    }

    @Nonnull
    public List<DiskPoolNode> getDiskPoolNodes() {
        return diskPoolNodes;
    }

    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.nodes_ExternalWorkspaceProperty_DisplayName();
        }
    }
}
