package org.jenkinsci.plugins.ewm.definitions;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

/**
 * @author Alexandru Somai
 *         date 5/24/16
 */
public class Disk implements Describable<Disk> {

    private final String diskId;
    private final String name;
    private final String physicalPathOnDisk;

    @DataBoundConstructor
    public Disk(String diskId, String name, String physicalPathOnDisk) {
        this.diskId = diskId;
        this.name = name;
        this.physicalPathOnDisk = physicalPathOnDisk;
    }

    @Override
    public Descriptor<Disk> getDescriptor() {
        return (DiskDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    public String getDiskId() {
        return diskId;
    }

    public String getName() {
        return name;
    }

    public String getPhysicalPathOnDisk() {
        return physicalPathOnDisk;
    }

    @Extension
    public static class DiskDescriptor extends Descriptor<Disk> {

        private String diskId;
        private String name;
        private String physicalPathOnDisk;

        public DiskDescriptor() {
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindJSON(this, formData);
            save();
            return super.configure(req, formData);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Disk";
        }

        public String getDiskId() {
            return diskId;
        }

        public void setDiskId(String diskId) {
            this.diskId = diskId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhysicalPathOnDisk() {
            return physicalPathOnDisk;
        }
        public void setPhysicalPathOnDisk(String physicalPathOnDisk) {
            this.physicalPathOnDisk = physicalPathOnDisk;
        }
    }
}
