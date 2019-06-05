package org.jenkinsci.plugins.ewm.clouds.Aws;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.List;

import static hudson.util.FormValidation.validateRequired;


public class EfsMountTarget implements Describable<EfsMountTarget> {
    private final String ipAddress;
    private final List<String> securityGroups;
    private final String subnetId;
    @DataBoundConstructor
    public EfsMountTarget(String ipAddress, List<String> securityGroups, String subnetId) {
        this.ipAddress = ipAddress;
        // TODO : should I use copy ? Is it a reference?
        this.securityGroups = securityGroups;
        this.subnetId = subnetId;
    }

    public String getIpAddress() { return ipAddress; }
    public String getSubnetId() { return subnetId; }
    public List<String> getSecurityGroups() { return securityGroups; }
    @Override
    public EfsMountTarget.DescriptorImpl getDescriptor() { return DESCRIPTOR; }

    @Extension
    public static final EfsMountTarget.DescriptorImpl DESCRIPTOR = new EfsMountTarget.DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<EfsMountTarget> {

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckSubnetId(@QueryParameter String value) {
            return validateRequired(value);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckIpAddress(@QueryParameter String value) {
            return validateRequired(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.definitions_EfsMountTarget_DisplayName();
        }
    }
}
