package org.jenkinsci.plugins.ewm.steps;

import hudson.Extension;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static hudson.Util.fixEmptyAndTrim;

/**
 * The 'exwsAllocate' step.
 * Computes an external workspace based on the globally defined disk pools and on the running job properties.
 *
 * @author Alexandru Somai
 */
public final class ExwsAllocateStep extends AbstractStepImpl {

    private final String diskPoolId;
    private String upstream;
    private RunWrapper selectedRun;

    public RunWrapper getSelectedRun() {
        return selectedRun;
    }

    @DataBoundSetter
    public void setSelectedRun(RunWrapper selectedRun) {
        this.selectedRun = selectedRun;
    }

    @DataBoundConstructor
    public ExwsAllocateStep(String diskPoolId) {
        this.diskPoolId = fixEmptyAndTrim(diskPoolId);
    }

    @CheckForNull
    public String getDiskPoolId() {
        return diskPoolId;
    }

    @CheckForNull
    public String getUpstream() {
        return upstream;
    }

    @DataBoundSetter
    public void setUpstream(String upstream) {
        this.upstream = fixEmptyAndTrim(upstream);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        private List<DiskPool> diskPools = new ArrayList<>();

        public DescriptorImpl() {
            super(ExwsAllocateExecution.class);
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            diskPools = req.bindJSONToList(DiskPool.class, formData.get("diskPools"));
            save();
            return super.configure(req, formData);
        }

        @Nonnull
        public List<DiskPool> getDiskPools() {
            return diskPools;
        }

        @Override
        public String getFunctionName() {
            return "exwsAllocate";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.steps_ExwsAllocateStep_DisplayName();
        }
    }
}
