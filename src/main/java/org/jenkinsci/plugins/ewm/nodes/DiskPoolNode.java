package org.jenkinsci.plugins.ewm.nodes;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.Util.fixNull;
import static hudson.util.FormValidation.validateRequired;

/**
 * Describable used to define disk pool properties for each {@link hudson.model.Node} configuration.
 *
 * @author Alexandru Somai
 */
public class DiskPoolNode implements Describable<DiskPoolNode> {

    private final String diskPoolRefId;
    private final List<DiskNode> diskNodes;

    @DataBoundConstructor
    public DiskPoolNode(String diskPoolRefId, List<DiskNode> diskNodes) {
        this.diskPoolRefId = fixEmptyAndTrim(diskPoolRefId);
        this.diskNodes = fixNull(diskNodes);
    }

    @CheckForNull
    public String getDiskPoolRefId() {
        return diskPoolRefId;
    }

    @Nonnull
    public List<DiskNode> getDiskNodes() {
        return diskNodes;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<DiskPoolNode> {

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckDiskPoolRefId(@QueryParameter String value) {
            return validateRequired(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.definitions_DiskPool_DisplayName();
        }
    }
}
