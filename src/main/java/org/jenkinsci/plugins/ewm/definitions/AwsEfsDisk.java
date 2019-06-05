package org.jenkinsci.plugins.ewm.definitions;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.ewm.DiskInfoProvider;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.ewm.clouds.Aws.EfsMountTarget;
import org.jenkinsci.plugins.ewm.clouds.Aws.EfsTag;
import org.jenkinsci.plugins.ewm.providers.NoDiskInfo;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;

import java.util.List;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.Util.isRelativePath;
import static hudson.util.FormValidation.validateRequired;

public class AwsEfsDisk extends Disk {
    private final Boolean isOnDemandEfs;
    private final Boolean isOnDemandMount;
    private final String efsId;
    private final String vpcId;
    private final String region;
    // when manually create EFS
    private final Boolean isEncrypted;
    private final String kmsKeyId;
    private final String performanceMode;
    private final String provisionedThroughput;
    private final String ThroughputMode;
    private final List<EfsTag> tags;

    // mount targets needed fields
    List<EfsMountTarget> mountTargets;


    // private final List<String> availabilityZones;
    @DataBoundConstructor
    public AwsEfsDisk(String diskId, String displayName, String masterMountPoint,
                      String physicalPathOnDisk, DiskInfoProvider diskInfo,
                      Boolean isExistingEfs, Boolean isOnDemandMount, String efsId,
                      String vpcId, String region, List<String> availabilityZones) {
        super(diskId, displayName, masterMountPoint, physicalPathOnDisk, diskInfo);
        this.isOnDemandMount = true;
        this.isOnDemandEfs = true;
        this.efsId = fixEmptyAndTrim(efsId);
        this.vpcId = fixEmptyAndTrim(vpcId);
        this.region = fixEmptyAndTrim(region);
    }


    @Override
    public Descriptor<Disk> getDescriptor() {
        return DESCRIPTOR;
    }

    public Boolean getIsOnDemandEfs() { return isOnDemandEfs; }

    public Boolean getIsOnDemandMount() { return isOnDemandMount; }

    public String getEfsId() { return efsId; }

    public String getVpcId() { return vpcId; }

    public String getRegion() { return region; }

    // TODO : is this an extension point ? Do I need to add it manually ?
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<Disk> {

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckEfsId(@QueryParameter String value, @QueryParameter Boolean isExsitingEfs) {
            if (isExsitingEfs) {
                return FormValidation.ok();
            }
            return validateRequired(value);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckVpcId(@QueryParameter String value, @QueryParameter Boolean isExsitingEfs) {
            if (isExsitingEfs) {
                return FormValidation.ok();
            }
            return validateRequired(value);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused")
        public FormValidation doCheckRegion(@QueryParameter String value, @QueryParameter Boolean isExsitingEfs) {
            if (isExsitingEfs) {
                return FormValidation.ok();
            }
            return validateRequired(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "AWS EFS Disk";
        }
    }
}
