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

import static hudson.Util.fixEmptyAndTrim;
import static hudson.util.FormValidation.validateRequired;

/**
 * Describable used to define disk properties for each {@link hudson.model.Node} configuration.
 *
 * @author Alexandru Somai
 */
public class NodeDisk implements Describable<NodeDisk> {

    private final String diskRefId;

    @Deprecated
    private transient String localRootPath;

    private String nodeMountPoint;

    @DataBoundConstructor
    public NodeDisk(String diskRefId, String nodeMountPoint) {
        this.diskRefId = fixEmptyAndTrim(diskRefId);
        this.nodeMountPoint = fixEmptyAndTrim(nodeMountPoint);
    }

    protected Object readResolve() {
        if (localRootPath != null) {
            nodeMountPoint = localRootPath;
        }
        return this;
    }

    @CheckForNull
    public String getDiskRefId() {
        return diskRefId;
    }

    @Deprecated
    public String getLocalRootPath() {
        return getNodeMountPoint();
    }

    @CheckForNull
    public String getNodeMountPoint() {
        return nodeMountPoint;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<NodeDisk> {

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckDiskRefId(@QueryParameter String value) {
            return validateRequired(value);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckNodeMountPoint(@QueryParameter String value) {
            return validateRequired(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.definitions_Disk_DisplayName();
        }
    }
}
