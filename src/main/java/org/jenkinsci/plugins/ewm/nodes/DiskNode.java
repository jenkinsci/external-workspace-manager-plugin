package org.jenkinsci.plugins.ewm.nodes;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * @author Alexandru Somai
 *         date 5/24/16
 */
public class DiskNode implements Describable<DiskNode> {

    private final String diskRefId;
    private final String localRootPath;

    @DataBoundConstructor
    public DiskNode(String diskRefId, String localRootPath) {
        this.diskRefId = diskRefId;
        this.localRootPath = localRootPath;
    }

    public String getDiskRefId() {
        return diskRefId;
    }

    public String getLocalRootPath() {
        return localRootPath;
    }

    @Override
    public DiskNodeDescriptor getDescriptor() {
        return (DiskNodeDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    @Extension
    public static class DiskNodeDescriptor extends Descriptor<DiskNode> {

        private String diskRefId;
        private String localRootPath;

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Disk";
        }

        public String getDiskRefId() {
            return diskRefId;
        }

        public void setDiskRefId(String diskRefId) {
            this.diskRefId = diskRefId;
        }

        public String getLocalRootPath() {
            return localRootPath;
        }

        public void setLocalRootPath(String localRootPath) {
            this.localRootPath = localRootPath;
        }
    }
}
