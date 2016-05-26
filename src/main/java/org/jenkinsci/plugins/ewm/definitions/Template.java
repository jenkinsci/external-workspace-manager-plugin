package org.jenkinsci.plugins.ewm.definitions;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.ewm.nodes.DiskNode;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Alexandru Somai
 *         date 5/24/16
 */
public class Template implements Describable<Template> {

    private final String diskPoolRefId;
    private final String label;
    private final List<DiskNode> diskNodes;

    @DataBoundConstructor
    public Template(String diskPoolRefId, String label, List<DiskNode> diskNodes) {
        this.diskPoolRefId = diskPoolRefId;
        this.label = label;
        this.diskNodes = diskNodes;
    }

    @Override
    public TemplateDescriptor getDescriptor() {
        return (TemplateDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    public String getDiskPoolRefId() {
        return diskPoolRefId;
    }

    public String getLabel() {
        return label;
    }

    public List<DiskNode> getDiskNodes() {
        return diskNodes;
    }

    @Extension
    public static class TemplateDescriptor extends Descriptor<Template> {

        private String diskRefId;
        private String label;
        private List<DiskNode> diskNodes;

        public TemplateDescriptor() {
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
            return "Template";
        }

        public String getDiskRefId() {
            return diskRefId;
        }

        public void setDiskRefId(String diskRefId) {
            this.diskRefId = diskRefId;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<DiskNode> getDiskNodes() {
            return diskNodes;
        }

        public void setDiskNodes(List<DiskNode> diskNodes) {
            this.diskNodes = diskNodes;
        }
    }
}
