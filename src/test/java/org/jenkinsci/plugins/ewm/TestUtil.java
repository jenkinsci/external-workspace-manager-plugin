package org.jenkinsci.plugins.ewm;

import hudson.util.ReflectionUtils;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.steps.ExwsAllocateStep;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for tests.
 *
 * @author Alexandru Somai
 */
public final class TestUtil {

    public static final String DISK_POOL_ID = "disk-pool-1";
    public static final String DISK_ID_ONE = "disk1";
    public static final String DISK_ID_TWO = "disk2";

    private TestUtil() {
        // do not instantiate
    }

    public static void setUpDiskPools(Jenkins jenkins, List<DiskPool> diskPools) {
        ExwsAllocateStep.DescriptorImpl descriptor = (ExwsAllocateStep.DescriptorImpl) jenkins.getDescriptor(ExwsAllocateStep.class);

        Field diskPoolsField = ReflectionUtils.findField(ExwsAllocateStep.DescriptorImpl.class, "diskPools");
        diskPoolsField.setAccessible(true);
        ReflectionUtils.setField(diskPoolsField, descriptor, diskPools);
    }

    public static Disk findAllocatedDisk(Disk... disks) {
        return Collections.max(Arrays.asList(disks), new Comparator<Disk>() {
            @Override
            public int compare(Disk disk1, Disk disk2) {
                long disk1UsableSpace = new File(disk1.getMasterMountPoint()).getUsableSpace();
                long disk2UsableSpace = new File(disk2.getMasterMountPoint()).getUsableSpace();

                return Long.compare(disk1UsableSpace, disk2UsableSpace);
            }
        });
    }
}
