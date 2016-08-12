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
import java.util.Collections;
import java.util.List;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.Util.fixNull;
import static hudson.util.FormValidation.validateRequired;

/**
 * Describable used to define disk pool properties for each {@link hudson.model.Node} configuration.
 *
 * @author Alexandru Somai
 */
public class NodeDiskPool implements Describable<NodeDiskPool> {

    private final String diskPoolRefId;
    private final List<NodeDisk> nodeDisks;

    @DataBoundConstructor
    public NodeDiskPool(String diskPoolRefId, List<NodeDisk> nodeDisks) {
        this.diskPoolRefId = fixEmptyAndTrim(diskPoolRefId);
        this.nodeDisks = fixNull(nodeDisks);
    }

    @CheckForNull
    public String getDiskPoolRefId() {
        return diskPoolRefId;
    }

    @Nonnull
    public List<NodeDisk> getNodeDisks() {
        return Collections.unmodifiableList(nodeDisks);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<NodeDiskPool> {

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
