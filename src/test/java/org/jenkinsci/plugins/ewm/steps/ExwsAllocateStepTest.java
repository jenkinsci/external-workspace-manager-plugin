package org.jenkinsci.plugins.ewm.steps;

import org.jenkinsci.plugins.ewm.TestUtil;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static hudson.model.Result.FAILURE;
import static java.lang.String.format;
import static org.jenkinsci.plugins.ewm.TestUtil.*;

/**
 * Unit tests for {@link ExwsAllocateStep}.
 *
 * @author Alexandru Somai
 */
public class ExwsAllocateStepTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();
    @ClassRule
    public static BuildWatcher watcher = new BuildWatcher();
    @ClassRule
    public static TemporaryFolder tmp1 = new TemporaryFolder();
    @ClassRule
    public static TemporaryFolder tmp2 = new TemporaryFolder();

    private static File pathToDisk1;
    private static File pathToDisk2;

    private WorkflowRun upstreamRun;
    private WorkflowRun downstreamRun;

    @BeforeClass
    public static void setUpPathToDisks() throws IOException {
        pathToDisk1 = tmp1.newFolder("mount-to-disk-one");
        pathToDisk2 = tmp2.newFolder("mount-to-disk-two");
    }

    @After
    public void tearDown() {
        removeDiskPools(j.jenkins);
    }

    /* ##### Tests for the upstream Job ###### */

    @Test
    public void missingDiskPoolId() throws Exception {
        createUpstreamJobAndRun(" ");

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains("Disk Pool ID was not provided as step parameter", upstreamRun);
    }

    @Test
    public void wrongDiskPoolId() throws Exception {
        setUpDiskPool();
        createUpstreamJobAndRun(DISK_POOL_ID + "random");

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("No Disk Pool ID matching '%s' was found in the global config", DISK_POOL_ID + "random"), upstreamRun);
    }

    @Test
    public void emptyDisks() throws Exception {
        setUpDiskPool();
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("No Disks were defined in the global config for Disk Pool ID '%s'", DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void missingDiskId() throws Exception {
        setUpDiskPool(new Disk("", "name", "mount", "path", null));
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("Disk ID was not provided in the Jenkins global config for the Disk Pool ID '%s'", DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void missingMasterMountPoint() throws Exception {
        setUpDiskPool(new Disk(DISK_ID_ONE, "name", "", "path", null));
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("Mounting point from Master to the disk is not defined for Disk ID '%s'", DISK_ID_ONE), upstreamRun);
    }

    @Test
    public void missingPhysicalPathOnDisk() throws Exception {
        setUpDiskPool(new Disk(DISK_ID_ONE, "name", "mount", "", null));
        createUpstreamJobAndRun();

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), upstreamRun);
        j.assertLogContains(format("The path on Disk is: %s", Paths.get(upstreamRun.getParent().getFullDisplayName(), Integer.toString(upstreamRun.getNumber()))), upstreamRun);
    }

    @Test
    public void physicalPathOnDiskNotRelative() throws Exception {
        setUpDiskPool(new Disk(DISK_ID_ONE, "name", "mount", "/path", null));
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("Physical path on disk defined for Disk ID '%s', within Disk Pool ID '%s' must be a relative path", DISK_ID_ONE, DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void successfullyAllocateWorkspace() throws Exception {
        Disk disk1 = new Disk(DISK_ID_ONE, "name", pathToDisk1.getPath(), "path", null);
        Disk disk2 = new Disk(DISK_ID_TWO, "name", pathToDisk2.getPath(), "path", null);
        setUpDiskPool(disk1, disk2);
        createUpstreamJobAndRun();

        Disk allocatedDisk = findAllocatedDisk(disk1, disk2);

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", allocatedDisk.getDiskId(), DISK_POOL_ID), upstreamRun);
        j.assertLogContains(format("The path on Disk is: %s", Paths.get(allocatedDisk.getPhysicalPathOnDisk(), upstreamRun.getParent().getFullDisplayName(), Integer.toString(upstreamRun.getNumber()))), upstreamRun);
    }

    /* ##### Tests for the downstream Job ###### */

    @Test
    public void upstreamJobHasNoActionRegistered() throws Exception {
        upstreamRun = createWorkflowJobAndRun("echo 'hello world'");
        String jobName = upstreamRun.getParent().getFullName();
        createDownstreamJobAndRun(jobName);

        j.assertBuildStatus(FAILURE, downstreamRun);
        j.assertLogContains(format("The selected run '%s' must have at least one call to the " +
                "exwsAllocate step in order to have a workspace usable by this job.", upstreamRun), downstreamRun);
    }

    @Test
    public void successfullyAllocateWorkspaceInDownstreamJob() throws Exception {
        Disk disk = new Disk(DISK_ID_ONE, "name", "mount", "path", null);
        setUpDiskPool(disk);
        createUpstreamJobAndRun();
        String upstreamName = upstreamRun.getParent().getName();
        createDownstreamJobAndRun(upstreamName);

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertBuildStatusSuccess(downstreamRun);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), downstreamRun);
        j.assertLogContains(format("The path on Disk is: %s", Paths.get(disk.getPhysicalPathOnDisk(), upstreamName, Integer.toString(upstreamRun.getNumber()))), downstreamRun);
    }

    @Test
    public void redundantParametersInTheDownstreamJob() throws Exception {
        Disk disk = new Disk(DISK_ID_ONE, "name", "mount", "path", null);
        setUpDiskPool(disk);

        createUpstreamJobAndRun();
        String upstreamName = upstreamRun.getParent().getName();
        downstreamRun = createWorkflowJobAndRun(format("" +
                "def run = selectRun job: '%s' \n" +
                "exwsAllocate diskPoolId: 'any-pool', selectedRun: run", upstreamName));

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertBuildStatusSuccess(downstreamRun);
        j.assertLogContains("WARNING: Both 'selectedRun' and 'diskPoolId' parameters were provided. " +
                "The 'diskPoolId' parameter will be ignored. The step will allocate the workspace used by the selected run.", downstreamRun);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), downstreamRun);
        j.assertLogContains(format("The path on Disk is: %s", Paths.get(disk.getPhysicalPathOnDisk(), upstreamName, Integer.toString(upstreamRun.getNumber()))), downstreamRun);
    }

    @Test
    public void upstreamJobRegisteredMultipleActions() throws Exception {
        Disk disk = new Disk(DISK_ID_ONE, "name", "mount", "path", null);
        DiskPool diskPool1 = new DiskPool("id1", "name", "desc", null, null, null, Collections.singletonList(disk));
        DiskPool diskPool2 = new DiskPool("id2", "name", "desc", null, null, null, Collections.singletonList(disk));
        setUpDiskPools(j.jenkins, diskPool1, diskPool2);

        upstreamRun = createWorkflowJobAndRun(format("" +
                " exwsAllocate diskPoolId: '%s' \n" +
                " exwsAllocate diskPoolId: '%s' ", diskPool1.getDiskPoolId(), diskPool2.getDiskPoolId()));
        String upstreamName = upstreamRun.getParent().getName();
        createDownstreamJobAndRun(upstreamName);

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertBuildStatusSuccess(downstreamRun);
        j.assertLogContains(format("WARNING: The selected run '%s' have recorded multiple external workspace allocations. " +
                "Did you call exwsAllocate step multiple times in the same run? This downstream Jenkins job will use the first recorded workspace allocation.", upstreamRun), downstreamRun);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, diskPool1.getDiskPoolId()), downstreamRun);
        j.assertLogContains(format("The path on Disk is: %s", Paths.get(disk.getPhysicalPathOnDisk(), upstreamName, Integer.toString(upstreamRun.getNumber()))), downstreamRun);
    }

    private void setUpDiskPool(Disk... disks) {
        DiskPool diskPool = new DiskPool(DISK_POOL_ID, "name", "desc", null, null, null, Arrays.asList(disks));
        setUpDiskPools(j.jenkins, diskPool);
    }

    private void createUpstreamJobAndRun() throws Exception {
        createUpstreamJobAndRun(DISK_POOL_ID);
    }

    private void createUpstreamJobAndRun(String diskPoolId) throws Exception {
        upstreamRun = createWorkflowJobAndRun(format("exwsAllocate diskPoolId: '%s'", diskPoolId));
    }

    private void createDownstreamJobAndRun(String upstreamName) throws Exception {
        downstreamRun = createWorkflowJobAndRun(format("" +
                "def run = selectRun job: '%s' \n" +
                "exwsAllocate selectedRun: run", upstreamName));
    }

    private static WorkflowRun createWorkflowJobAndRun(String script) throws Exception {
        return TestUtil.createWorkflowJobAndRun(j.jenkins, script);
    }
}
