package org.jenkinsci.plugins.ewm.steps;

import hudson.Extension;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.ewm.DiskAllocationStrategy;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.utils.FormValidationUtil;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static hudson.Util.fixEmptyAndTrim;

/**
 * The 'exwsAllocate' step.
 * Computes an external workspace based on the globally defined disk pools and on the running job properties.
 *
 * @author Alexandru Somai
 */
public final class ExwsAllocateStep extends AbstractStepImpl {

    @CheckForNull
    private final String diskPoolId;

    @CheckForNull
    private RunWrapper selectedRun;

    @CheckForNull
    private String path;

    @CheckForNull
    private DiskAllocationStrategy strategy;

    @DataBoundConstructor
    public ExwsAllocateStep(String diskPoolId) {
        this.diskPoolId = fixEmptyAndTrim(diskPoolId);
    }

    @CheckForNull
    public String getDiskPoolId() {
        return diskPoolId;
    }

    @CheckForNull
    public RunWrapper getSelectedRun() {
        return selectedRun;
    }

    @DataBoundSetter
    public void setSelectedRun(RunWrapper selectedRun) {
        this.selectedRun = selectedRun;
    }

    @CheckForNull
    public String getPath() {
        return path;
    }

    @DataBoundSetter
    public void setPath(String path) {
        this.path = fixEmptyAndTrim(path);
    }

    @CheckForNull
    public DiskAllocationStrategy getStrategy() {
        return strategy;
    }

    @DataBoundSetter
    public void setStrategy(DiskAllocationStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        private List<DiskPool> diskPools = Collections.emptyList();

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
            return Collections.unmodifiableList(diskPools);
        }

        public void setDiskPools(List<DiskPool> diskPools) {
            this.diskPools.clear();
            this.diskPools.addAll(diskPools);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckPath(@QueryParameter String value) {
            return FormValidationUtil.validateWorkspaceTemplate(value);
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
