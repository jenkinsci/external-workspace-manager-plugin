package org.jenkinsci.plugins.ewm;

import hudson.model.queue.QueueTaskFuture;
import hudson.util.ReflectionUtils;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.steps.ExwsAllocateStep;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

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

    public static WorkflowRun createWorkflowJobAndRun(Jenkins jenkins, String name, String script) throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, name);
        job.setDefinition(new CpsFlowDefinition(script, true));
        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0);
        assertThat(runFuture, notNullValue());

        return runFuture.get();
    }

    public static void setupDiskPool(Jenkins jenkins, String diskPoolId, Disk... disks) {
        ExwsAllocateStep.DescriptorImpl descriptor = (ExwsAllocateStep.DescriptorImpl) jenkins.getDescriptor(ExwsAllocateStep.class);

        List<DiskPool> diskPools = new ArrayList<>();
        DiskPool diskPool = new DiskPool(diskPoolId, "name", "description", Arrays.asList(disks));
        diskPools.add(diskPool);

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
