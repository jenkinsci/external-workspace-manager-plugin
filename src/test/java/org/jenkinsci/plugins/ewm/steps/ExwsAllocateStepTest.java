package org.jenkinsci.plugins.ewm.steps;

import hudson.model.FreeStyleBuild;
import org.jenkinsci.plugins.ewm.TestUtil;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;

import static hudson.model.Result.FAILURE;
import static java.lang.String.format;
import static org.jenkinsci.plugins.ewm.TestUtil.*;

/**
 * Unit tests for {@link ExwsAllocateStep}.
 *
 * @author Alexandru Somai
 */
public class ExwsAllocateStepTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();
    @Rule
    public TemporaryFolder tmp1 = new TemporaryFolder();
    @Rule
    public TemporaryFolder tmp2 = new TemporaryFolder();

    private File pathToDisk1;
    private File pathToDisk2;

    @Before
    public void setUpPathToDisks() throws IOException {
        pathToDisk1 = tmp1.newFolder("mount-to-disk-one");
        pathToDisk2 = tmp2.newFolder("mount-to-disk-two");
    }

    private WorkflowRun upstreamRun;
    private WorkflowRun downstreamRun;

    /* ##### Tests for the upstream Job ###### */

    @Test
    public void missingDiskPoolId() throws Exception {
        createUpstreamJobAndRun(" ");

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains("Disk Pool ID was not provided as step parameter", upstreamRun);
    }

    @Test
    public void wrongDiskPoolId() throws Exception {
        setupDiskPool();
        createUpstreamJobAndRun(DISK_POOL_ID + "random");

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("No Disk Pool ID matching '%s' was found in the global config", DISK_POOL_ID + "random"), upstreamRun);
    }

    @Test
    public void emptyDisks() throws Exception {
        setupDiskPool();
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("No Disks were defined in the global config for Disk Pool ID '%s'", DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void missingDiskId() throws Exception {
        setupDiskPool(new Disk("", "name", "mount", "path"));
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("Disk ID was not provided in the Jenkins global config for the Disk Pool ID '%s'", DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void missingMasterMountPoint() throws Exception {
        setupDiskPool(new Disk(DISK_ID_ONE, "name", "", "path"));
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("Mounting point from Master to the disk is not defined for Disk ID '%s', from Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void missingPhysicalPathOnDisk() throws Exception {
        setupDiskPool(new Disk(DISK_ID_ONE, "name", "mount", ""));
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("Physical path on disk was not provided in the Jenkins global config for Disk ID: '%s', within Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void physicalPathOnDiskNotRelative() throws Exception {
        setupDiskPool(new Disk(DISK_ID_ONE, "name", "mount", "/path"));
        createUpstreamJobAndRun();

        j.assertBuildStatus(FAILURE, upstreamRun);
        j.assertLogContains(format("Physical path on disk defined for Disk ID '%s', within Disk Pool ID '%s' must be a relative path", DISK_ID_ONE, DISK_POOL_ID), upstreamRun);
    }

    @Test
    public void successfullyAllocateWorkspace() throws Exception {
        Disk disk1 = new Disk(DISK_ID_ONE, "name", pathToDisk1.getPath(), "path");
        Disk disk2 = new Disk(DISK_ID_TWO, "name", pathToDisk2.getPath(), "path");
        setupDiskPool(disk1, disk2);
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
        j.assertLogContains(format("There isn't any upstream job associated with '%s'", upstreamName), downstreamRun);
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
        String jobName = "test";
        createWorkflowJobAndRun(j.jenkins, jobName, "echo 'hello world'");
        createDownstreamJobAndRun(jobName);

        j.assertBuildStatus(FAILURE, downstreamRun);
        j.assertLogContains(format("The Jenkins job '%s' does not have registered any 'External Workspace Allocate' action. Did you run exwsAllocate step in the upstream job?", jobName), downstreamRun);
    }

    @Test
    public void upstreamBuildIsNotPipelineJob() throws Exception {
        String jobName = "name";
        FreeStyleBuild build = j.createFreeStyleProject(jobName).scheduleBuild2(0).get();
        createDownstreamJobAndRun(jobName);

        j.assertBuildStatus(FAILURE, downstreamRun);
        j.assertLogContains(format("Build '%s' is not a Pipeline job. Can't read the run actions", build), downstreamRun);
    }

    @Test
    public void successfullyAllocateWorkspaceInDownstreamJob() throws Exception {
        Disk disk = new Disk(DISK_ID_ONE, "name", "mount", "path");
        setupDiskPool(disk);
        createUpstreamJobAndRun();
        String upstreamName = upstreamRun.getParent().getName();
        createDownstreamJobAndRun(upstreamName);

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertBuildStatusSuccess(downstreamRun);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), downstreamRun);
        j.assertLogContains(format("The path on Disk is: %s/%s/%d", disk.getPhysicalPathOnDisk(), upstreamName, upstreamRun.getNumber()), downstreamRun);
    }

    private void setupDiskPool(Disk... disks) {
        TestUtil.setupDiskPool(j.jenkins, DISK_POOL_ID, disks);
    }

    private void createUpstreamJobAndRun() throws Exception {
        createUpstreamJobAndRun(DISK_POOL_ID);
    }

    private void createUpstreamJobAndRun(String diskPoolId) throws Exception {
        upstreamRun = createWorkflowJobAndRun(j.jenkins, "upstream-job", format("exwsAllocate diskPoolId: '%s'", diskPoolId));
    }

    private void createDownstreamJobAndRun(String upstreamName) throws Exception {
        downstreamRun = createWorkflowJobAndRun(j.jenkins, "downstream-job", format("exwsAllocate upstream: '%s'", upstreamName));
    }
}
