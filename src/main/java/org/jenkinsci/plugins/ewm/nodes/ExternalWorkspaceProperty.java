package org.jenkinsci.plugins.ewm.nodes;

import hudson.Extension;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.util.FormValidationUtil;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static hudson.Util.fixEmptyAndTrim;

/**
 * A {@link NodeProperty} where are defined the {@link DiskNode} definitions.
 *
 * @author Alexandru Somai
 */
public class ExternalWorkspaceProperty extends NodeProperty<Node> {

    private final String diskPoolRefId;
    private final List<DiskNode> diskNodes;

    @DataBoundConstructor
    public ExternalWorkspaceProperty(String diskPoolRefId, List<DiskNode> diskNodes) {
        this.diskPoolRefId = fixEmptyAndTrim(diskPoolRefId);
        this.diskNodes = diskNodes != null ? diskNodes : new ArrayList<DiskNode>();
    }

    @CheckForNull
    public String getDiskPoolRefId() {
        return diskPoolRefId;
    }

    public List<DiskNode> getDiskNodes() {
        return diskNodes;
    }

    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {

        public FormValidation doCheckDiskPoolRefId(@QueryParameter String value) {
            return FormValidationUtil.doCheckValue(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.nodes_ExternalWorkspaceProperty_DisplayName();
        }
    }
}
