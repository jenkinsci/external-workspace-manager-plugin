package org.jenkinsci.plugins.ewm.nodes;

import hudson.Extension;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Alexandru Somai
 *         date 5/24/16
 */
public class ExternalWorkspaceProperty extends NodeProperty<Node> {

    private final String diskPoolRefId;
    private final List<DiskNode> diskNodes;

    @DataBoundConstructor
    public ExternalWorkspaceProperty(String diskPoolRefId, List<DiskNode> diskNodes) {
        this.diskPoolRefId = diskPoolRefId;
        this.diskNodes = diskNodes;
    }

    public String getDiskPoolRefId() {
        return diskPoolRefId;
    }

    public List<DiskNode> getDiskNodes() {
        return diskNodes;
    }

    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {

        private String diskPoolRefId;
        private List<DiskNode> diskNodes;

        public static List<DiskNode.DiskNodeDescriptor> getDescriptors() {
            return Jenkins.getInstance().getDescriptorList(DiskNode.class);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "External Workspace";
        }

        public List<DiskNode> getDiskNodes() {
            return diskNodes;
        }

        public void setDiskNodes(List<DiskNode> diskNodes) {
            this.diskNodes = diskNodes;
        }

        public String getDiskPoolRefId() {
            return diskPoolRefId;
        }

        public void setDiskPoolRefId(String diskPoolRefId) {
            this.diskPoolRefId = diskPoolRefId;
        }
    }
}
