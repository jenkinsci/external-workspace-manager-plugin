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
public class ExternalWorkspaceDefinition implements Describable<ExternalWorkspaceDefinition> {

    private final List<DiskPool> diskPools;

    @DataBoundConstructor
    public ExternalWorkspaceDefinition(List<DiskPool> diskPools) {
        this.diskPools = diskPools;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    public List<DiskPool> getDiskPools() {
        return diskPools;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ExternalWorkspaceDefinition> {

        private List<DiskPool> diskPools;

        public DescriptorImpl() {
            load();
        }

        public static List<DiskPool.DiskPoolDescriptor> getDescriptors() {
            return Jenkins.getInstance().getDescriptorList(DiskPool.class);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindJSON(this, formData);
            save();
            return super.configure(req, formData);
        }

        public List<DiskPool> getDiskPools() {
            return diskPools;
        }

        public void setDiskPools(List<DiskPool> diskPools) {
            this.diskPools = diskPools;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "External Workspace Definition";
        }
    }
}
