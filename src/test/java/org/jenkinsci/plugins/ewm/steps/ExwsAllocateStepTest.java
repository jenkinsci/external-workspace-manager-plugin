package org.jenkinsci.plugins.ewm.steps;

import hudson.model.queue.QueueTaskFuture;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static hudson.model.Result.FAILURE;
import static java.lang.String.format;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.notNullValue;
import static org.jenkinsci.plugins.ewm.TestUtil.*;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link ExwsAllocateStep}.
 *
 * @author Alexandru Somai
 */
public class ExwsAllocateStepTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();
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
        setUpDiskPools(j.jenkins, Collections.<DiskPool>emptyList());
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
        setUpDiskPool(new Disk("", "name", "mount", "path"));
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("Disk ID was not provided in the Jenkins global config for the Disk Pool ID '%s'", DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void missingMasterMountPoint() throws Exception {
        setUpDiskPool(new Disk(DISK_ID_ONE, "name", "", "path"));
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("Mounting point from Master to the disk is not defined for Disk ID '%s', from Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void missingPhysicalPathOnDisk() throws Exception {
        setUpDiskPool(new Disk(DISK_ID_ONE, "name", "mount", ""));
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("Physical path on disk was not provided in the Jenkins global config for Disk ID: '%s', within Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void physicalPathOnDiskNotRelative() throws Exception {
        setUpDiskPool(new Disk(DISK_ID_ONE, "name", "mount", "/path"));
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("Physical path on disk defined for Disk ID '%s', within Disk Pool ID '%s' must be a relative path", DISK_ID_ONE, DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void successfullyAllocateWorkspace() throws Exception {
        Disk disk1 = new Disk(DISK_ID_ONE, "name", pathToDisk1.getPath(), "path");
        Disk disk2 = new Disk(DISK_ID_TWO, "name", pathToDisk2.getPath(), "path");
        setUpDiskPool(disk1, disk2);
        createUpstreamJobAndRun();

        Disk allocatedDisk = findAllocatedDisk(disk1, disk2);

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", allocatedDisk.getDiskId(), DISK_POOL_ID), upstreamRun);
        j.assertLogContains(format("The path on Disk is: %s/%s/%d", allocatedDisk.getPhysicalPathOnDisk(), upstreamRun.getParent().getFullDisplayName(), upstreamRun.getNumber()), upstreamRun);
    }

    /* ##### Tests for the downstream Job ###### */

    @Test
    public void downstreamJobWithInvalidUpstreamName() throws Exception {
        String upstreamName = "random-upstream-name";
        createDownstreamJobAndRun(upstreamName);

        j.assertBuildStatus(FAILURE, downstreamRun);
        j.assertLogContains(format("Can't find any upstream Jenkins job by the full name '%s'. Are you sure that this is the full project name?", upstreamName), downstreamRun);
    }

    @Test
    public void upstreamJobHasNoStableBuild() throws Exception {
        createUpstreamJobAndRun("random-disk-pool");
        String upstreamName = upstreamRun.getParent().getName();
        createDownstreamJobAndRun(upstreamName);

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertBuildStatus(FAILURE, downstreamRun);
        j.assertLogContains(format("'%s' doesn't have any stable build", upstreamName), downstreamRun);
    }

    @Test
    public void upstreamJobHasNoActionRegistered() throws Exception {
        String jobName = randomAlphanumeric(10);
        upstreamRun = createWorkflowJobAndRun(jobName, "echo 'hello world'");
        createDownstreamJobAndRun(jobName);

        j.assertBuildStatus(FAILURE, downstreamRun);
        j.assertLogContains(format("This downstream job has to allocate the same external workspace used by the upstream job. " +
                "To do so, the exwsAllocate step must be called in the upstream job. " +
                "Please call the exwsAllocate step in the upstream job, because the build '%s' doesn't have such calls.", upstreamRun), downstreamRun);
    }

    @Test
    public void successfullyAllocateWorkspaceInDownstreamJob() throws Exception {
        Disk disk = new Disk(DISK_ID_ONE, "name", "mount", "path");
        setUpDiskPool(disk);
        createUpstreamJobAndRun();
        String upstreamName = upstreamRun.getParent().getName();
        createDownstreamJobAndRun(upstreamName);

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertBuildStatusSuccess(downstreamRun);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), downstreamRun);
        j.assertLogContains(format("The path on Disk is: %s/%s/%d", disk.getPhysicalPathOnDisk(), upstreamName, upstreamRun.getNumber()), downstreamRun);
    }

    @Test
    public void redundantParametersInTheDownstreamJob() throws Exception {
        Disk disk = new Disk(DISK_ID_ONE, "name", "mount", "path");
        setUpDiskPool(disk);

        createUpstreamJobAndRun();
        String upstreamName = upstreamRun.getParent().getName();
        downstreamRun = createWorkflowJobAndRun(randomAlphanumeric(10), format("" +
                "exwsAllocate diskPoolId: 'any-pool', upstream: '%s'", upstreamName));

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertBuildStatusSuccess(downstreamRun);
        j.assertLogContains("WARNING: Both 'upstream' and 'diskPoolId' parameters were provided. " +
                "The 'diskPoolId' parameter will be ignored. The step will allocate the workspace used by the upstream job.", downstreamRun);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), downstreamRun);
        j.assertLogContains(format("The path on Disk is: %s/%s/%d", disk.getPhysicalPathOnDisk(), upstreamName, upstreamRun.getNumber()), downstreamRun);
    }

    @Test
    public void upstreamJobRegisteredMultipleActions() throws Exception {
        Disk disk = new Disk(DISK_ID_ONE, "name", "mount", "path");
        DiskPool diskPool1 = new DiskPool("id1", "name", "desc", Collections.singletonList(disk));
        DiskPool diskPool2 = new DiskPool("id2", "name", "desc", Collections.singletonList(disk));
        setUpDiskPools(j.jenkins, Arrays.asList(diskPool1, diskPool2));

        upstreamRun = createWorkflowJobAndRun(randomAlphanumeric(10), format("" +
                " exwsAllocate diskPoolId: '%s' \n" +
                " exwsAllocate diskPoolId: '%s' ", diskPool1.getDiskPoolId(), diskPool2.getDiskPoolId()));
        String upstreamName = upstreamRun.getParent().getName();
        createDownstreamJobAndRun(upstreamName);

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertBuildStatusSuccess(downstreamRun);
        j.assertLogContains(format("WARNING: The Jenkins job '%s' have recorded multiple external workspace allocations. " +
                "Did you call exwsAllocate step multiple times in the same run? This downstream Jenkins job will use the first recorded workspace allocation.", upstreamName), downstreamRun);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, diskPool1.getDiskPoolId()), downstreamRun);
        j.assertLogContains(format("The path on Disk is: %s/%s/%d", disk.getPhysicalPathOnDisk(), upstreamName, upstreamRun.getNumber()), downstreamRun);
    }

    private void setUpDiskPool(Disk... disks) {
        DiskPool diskPool = new DiskPool(DISK_POOL_ID, "name", "desc", Arrays.asList(disks));
        setUpDiskPools(j.jenkins, Collections.singletonList(diskPool));
    }

    private void createUpstreamJobAndRun() throws Exception {
        createUpstreamJobAndRun(DISK_POOL_ID);
    }

    private void createUpstreamJobAndRun(String diskPoolId) throws Exception {
        upstreamRun = createWorkflowJobAndRun("upstream-" + randomAlphanumeric(10), format("exwsAllocate diskPoolId: '%s'", diskPoolId));
    }

    private void createDownstreamJobAndRun(String upstreamName) throws Exception {
        downstreamRun = createWorkflowJobAndRun("downstream-" + randomAlphanumeric(10), format("exwsAllocate upstream: '%s'", upstreamName));
    }

    private WorkflowRun createWorkflowJobAndRun(String name, String script) throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, name);
        job.setDefinition(new CpsFlowDefinition(script, true));
        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0);
        assertThat(runFuture, notNullValue());

        return runFuture.get();
    }
}
