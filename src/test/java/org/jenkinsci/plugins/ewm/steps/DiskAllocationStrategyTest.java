package org.jenkinsci.plugins.ewm.steps;

import hudson.model.Result;
import org.jenkinsci.plugins.ewm.TestUtil;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.providers.UserProvidedDiskInfo;
import org.jenkinsci.plugins.ewm.strategies.FastestReadSpeedStrategy;
import org.jenkinsci.plugins.ewm.strategies.FastestWriteSpeedStrategy;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import static java.lang.String.format;

/**
 * Tests for the Disk Allocation Strategy feature used in the Pipeline context.
 *
 * @author Alexandru Somai
 */
public class DiskAllocationStrategyTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @ClassRule
    public static BuildWatcher watcher = new BuildWatcher();

    @After
    public void tearDown() {
        TestUtil.removeDiskPools(j.jenkins);
    }

    @Test
    public void useAllocationStrategyInPipeline() throws Exception {
        Disk disk1 = TestUtil.createDisk(new UserProvidedDiskInfo(0, 1));
        Disk disk2 = TestUtil.createDisk(new UserProvidedDiskInfo(0, 3));
        DiskPool diskPool = TestUtil.createDiskPool(disk1, disk2);
        TestUtil.setUpDiskPools(j.jenkins, diskPool);

        WorkflowRun run = TestUtil.createWorkflowJobAndRun(j.jenkins, format("" +
                "exwsAllocate diskPoolId: '%s', strategy: fastestWriteSpeed()", diskPool.getDiskPoolId()));

        j.assertBuildStatusSuccess(run);
        j.assertLogContains(format("Using Disk allocation strategy: '%s'", new FastestWriteSpeedStrategy().getDescriptor().getDisplayName()), run);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", disk2.getDiskId(), diskPool.getDiskPoolId()), run);
    }

    @Test
    public void diskHasNotEnoughUsableSpace() throws Exception {
        // The Disk mounting point parameter isn't an actual mount.
        // Therefore, the disk's usable space will be 0, and the job will fail as expected
        Disk disk = new Disk("disk", null, "not-an-actual-mounting-point", null, null);
        DiskPool diskPool = TestUtil.createDiskPool(disk);
        TestUtil.setUpDiskPools(j.jenkins, diskPool);
        long estimatedWorkspaceSize = 300;

        WorkflowRun run = TestUtil.createWorkflowJobAndRun(j.jenkins, format("" +
                        "exwsAllocate diskPoolId: '%s', " +
                        " strategy: fastestWriteSpeed(estimatedWorkspaceSize: %s)",
                diskPool.getDiskPoolId(), estimatedWorkspaceSize));

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains(format("Using Disk allocation strategy: '%s'", new FastestWriteSpeedStrategy().getDescriptor().getDisplayName()), run);
        j.assertLogContains(format("Couldn't find any Disk with at least %s MB usable space", estimatedWorkspaceSize), run);
    }

    @Test
    public void fallbackToGlobalAllocationStrategy() throws Exception {
        Disk disk1 = TestUtil.createDisk(new UserProvidedDiskInfo(1, 0));
        Disk disk2 = TestUtil.createDisk(new UserProvidedDiskInfo(3, 0));
        FastestReadSpeedStrategy strategy = new FastestReadSpeedStrategy();
        DiskPool diskPool = TestUtil.createDiskPool(strategy, disk1, disk2);
        TestUtil.setUpDiskPools(j.jenkins, diskPool);

        WorkflowRun run = TestUtil.createWorkflowJobAndRun(j.jenkins, format("" +
                "exwsAllocate diskPoolId: '%s'", diskPool.getDiskPoolId()));

        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Disk allocation strategy was not provided as step parameter. Fallback to the strategy defined in the Jenkins global config", run);
        j.assertLogContains(format("Using Disk allocation strategy: '%s'", strategy.getDescriptor().getDisplayName()), run);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", disk2.getDiskId(), diskPool.getDiskPoolId()), run);
    }
}
