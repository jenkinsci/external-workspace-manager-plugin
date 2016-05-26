package org.jenkinsci.plugins.ewm.definitions;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Alexandru Somai
 *         date 5/24/16
 */
public class DiskPool implements Describable<DiskPool> {

    private final String diskPoolId;
    private final String name;
    private final String description;
    private final List<Disk> disks;

    @DataBoundConstructor
    public DiskPool(String diskPoolId, String name, String description, List<Disk> disks) {
        this.diskPoolId = diskPoolId;
        this.name = name;
        this.description = description;
        this.disks = disks;
    }

    @Override
    public DiskPoolDescriptor getDescriptor() {
        return (DiskPoolDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    public String getDiskPoolId() {
        return diskPoolId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Disk> getDisks() {
        return disks;
    }

    @Extension
    public static class DiskPoolDescriptor extends Descriptor<DiskPool> {

        private String diskPoolId;
        private String name;
        private String description;
        private List<Disk> disks;

        public DiskPoolDescriptor() {
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
            return "Disk Pool";
        }

        public String getDiskPoolId() {
            return diskPoolId;
        }

        public void setDiskPoolId(String diskPoolId) {
            this.diskPoolId = diskPoolId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<Disk> getDisks() {
            return disks;
        }

        public void setDisks(List<Disk> disks) {
            this.disks = disks;
        }
    }
}
