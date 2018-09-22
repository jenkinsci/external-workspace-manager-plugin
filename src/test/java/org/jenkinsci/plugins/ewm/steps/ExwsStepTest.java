package org.jenkinsci.plugins.ewm.steps;

import hudson.model.Node;
import hudson.model.queue.QueueTaskFuture;
import hudson.util.ReflectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.definitions.Template;
import org.jenkinsci.plugins.ewm.nodes.NodeDisk;
import org.jenkinsci.plugins.ewm.nodes.NodeDiskPool;
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
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static hudson.model.Result.FAILURE;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.countMatches;
import org.apache.commons.io.FilenameUtils;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.jenkinsci.plugins.ewm.TestUtil.*;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link ExwsStep}.
 *
 * @author Alexandru Somai
 */
public class ExwsStepTest {

    private static final String TEXT = "Write random text to a file";

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();
    @ClassRule
    public static TemporaryFolder tmp1 = new TemporaryFolder();
    @ClassRule
    public static TemporaryFolder tmp2 = new TemporaryFolder();

    private static Node node1;
    private static Node node2;

    private static Disk disk1;
    private static Disk disk2;
    private static NodeDisk nodeDisk1;
    private static NodeDisk nodeDisk2;

    private static WorkflowJob job;

    private WorkflowRun run;

    @BeforeClass
    public static void setUp() throws Exception {
        node1 = j.createSlave("node-one", "linux", null);
        node2 = j.createSlave("node-two", "test", null);

        File pathToDisk1 = tmp1.newFolder("mount-to-disk-one");
        File pathToDisk2 = tmp2.newFolder("mount-to-disk-two");

        disk1 = new Disk(DISK_ID_ONE, "name one", pathToDisk1.getPath(), FilenameUtils.separatorsToSystem("jenkins-project/disk-one"), null);
        disk2 = new Disk(DISK_ID_TWO, "name two", pathToDisk2.getPath(), FilenameUtils.separatorsToSystem("jenkins-project/disk-two"), null);
        DiskPool diskPool = new DiskPool(DISK_POOL_ID, "name", "desc", null, null, null, Arrays.asList(disk1, disk2));
        setUpDiskPools(j.jenkins, diskPool);

        nodeDisk1 = new NodeDisk(DISK_ID_ONE, pathToDisk1.getPath());
        nodeDisk2 = new NodeDisk(DISK_ID_TWO, pathToDisk2.getPath());

        job = createWorkflowJob();
    }

    @After
    public void tearDown() throws IOException {
        resetTemplates();
        removeExternalWorkspaceNodeProperty(node1);
        removeExternalWorkspaceNodeProperty(node2);
    }

    @Test
    public void missingExternalWorkspaceParam() throws Exception {
        WorkflowJob wrongJob = createWorkflowJob("" +
                " node('linux') { \n" +
                "   exws() { } \n" +
                " } ");
        runWorkflowJob(wrongJob);

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains("No external workspace provided. Did you run the exwsAllocate step?", run);
    }

    @Test
    public void missingDiskPoolRefIdFromTemplate() throws Exception {
        setUpTemplates(new Template("linux", Collections.<NodeDiskPool>emptyList()));

        runWorkflowJob(job);

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(String.format("No Disk Pool Ref ID matching '%s' was found in the External Workspace Template config labeled 'linux'", DISK_POOL_ID), run);
    }

    @Test
    public void missingNodeExternalWorkspaceProperty() throws Exception {
        runWorkflowJob(job);

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains("There is no External Workspace config defined in Node 'node-one' config", run);
    }

    @Test
    public void missingDiskPoolRefIdFromNodeProperty() throws Exception {
        addExternalWorkspaceNodeProperty(node1, "", nodeDisk1, nodeDisk2);

        runWorkflowJob(job);

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(format("No Disk Pool Ref ID matching '%s' was found in Node 'node-one' config", DISK_POOL_ID), run);
    }

    @Test
    public void missingDiskRefIdFromNodeProperty() throws Exception {
        addExternalWorkspaceNodeProperty(node1, DISK_POOL_ID, new NodeDisk("", "local-path"));

        runWorkflowJob(job);

        Disk selectedDisk = findAllocatedDisk(disk1, disk2);
        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(format("The Node 'node-one' config does not have defined any Disk Ref ID '%s'", selectedDisk.getDiskId()), run);
    }

    @Test
    public void missingNodeMountPointFromNodeProperty() throws Exception {
        addExternalWorkspaceNodeProperty(node1, DISK_POOL_ID, new NodeDisk(DISK_ID_ONE, ""));

        runWorkflowJob(job);

        Disk selectedDisk = findAllocatedDisk(disk1, disk2);
        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(format("The Node 'node-one' config does not have defined any node mount point for Disk Ref ID '%s'", selectedDisk.getDiskId()), run);
    }

    @Test
    public void sharedWorkspaceBetweenTwoDifferentNodes() throws Exception {
        addExternalWorkspaceNodeProperty(node1, DISK_POOL_ID, nodeDisk1, nodeDisk2);
        addExternalWorkspaceNodeProperty(node2, DISK_POOL_ID, nodeDisk1, nodeDisk2);

        WorkflowJob jobWithTwoNodes = createWorkflowJobWithTwoNodes();
        runWorkflowJob(jobWithTwoNodes);
        Disk allocatedDisk = findAllocatedDisk(disk1, disk2);

        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Searching for disk definitions in the External Workspace Templates from Jenkins global config", run);
        j.assertLogContains("Searching for disk definitions in the Node config", run);
        j.assertLogContains(format("Running in %s", Paths.get(
            allocatedDisk.getMasterMountPoint(),
            allocatedDisk.getPhysicalPathOnDisk(),
            run.getParent().getName(),
            Integer.toString(run.getNumber()))),
            run);
        // The text written to file should be printed twice on the console output (when writing and when reading the file)
        assertThat(countMatches(JenkinsRule.getLog(run), TEXT), is(2));
    }

    @Test
    public void sharedWorkspaceBetweenTwoDifferentNodesWithTemplate() throws Exception {
        NodeDiskPool nodeDiskPool = new NodeDiskPool(DISK_POOL_ID, Arrays.asList(nodeDisk1, nodeDisk2));
        Template linuxTemplate = new Template("linux", Collections.singletonList(nodeDiskPool));
        Template testTemplate = new Template("test", Collections.singletonList(nodeDiskPool));
        setUpTemplates(linuxTemplate, testTemplate);

        WorkflowJob jobWithTwoNodes = createWorkflowJobWithTwoNodes();
        runWorkflowJob(jobWithTwoNodes);
        Disk allocatedDisk = findAllocatedDisk(disk1, disk2);

        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Searching for disk definitions in the External Workspace Templates from Jenkins global config", run);
        j.assertLogNotContains("Searching for disk definitions in the Node config", run);
        j.assertLogContains(format("Running in %s", Paths.get(
            allocatedDisk.getMasterMountPoint(),
            allocatedDisk.getPhysicalPathOnDisk(),
            run.getParent().getName(),
            Integer.toString(run.getNumber()))),
            run);
        // The text written to file should be printed twice on the console output (when writing and when reading the file)
        assertThat(countMatches(JenkinsRule.getLog(run), TEXT), is(2));
    }

    @Test
    public void twoDifferentJobsUsingTheSameWorkspace() throws Exception {
        addExternalWorkspaceNodeProperty(node1, DISK_POOL_ID, nodeDisk1, nodeDisk2);
        addExternalWorkspaceNodeProperty(node2, DISK_POOL_ID, nodeDisk1, nodeDisk2);

        WorkflowJob upstreamJob = createWorkflowJob();
        WorkflowRun upstreamRun = runWorkflowJob(upstreamJob);
        Disk allocatedDisk = findAllocatedDisk(disk1, disk2);

        WorkflowJob downstreamJob = createDownstreamWorkflowJob(upstreamJob.getFullName());
        WorkflowRun downstreamRun = runWorkflowJob(downstreamJob);

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertBuildStatusSuccess(downstreamRun);
        j.assertLogContains("Searching for disk definitions in the External Workspace Templates from Jenkins global config", downstreamRun);
        j.assertLogContains("Searching for disk definitions in the Node config", downstreamRun);
        j.assertLogContains(format("Running in %s", Paths.get(
            allocatedDisk.getMasterMountPoint(),
            allocatedDisk.getPhysicalPathOnDisk(),
            upstreamJob.getFullDisplayName(),
            Integer.toString(upstreamRun.getNumber()))),
            downstreamRun);
        // The text written to file in the upstream job should be printed in the downstream job
        j.assertLogContains("foo", downstreamRun);
    }

    private static void resetTemplates() {
        setUpTemplates(Collections.<Template>emptyList());
    }

    private static void setUpTemplates(Template... templates) {
        setUpTemplates(Arrays.asList(templates));
    }

    private static void setUpTemplates(List<Template> templates) {
        ExwsStep.DescriptorImpl descriptor = (ExwsStep.DescriptorImpl) j.jenkins.getDescriptor(ExwsStep.class);

        Field templatesField = ReflectionUtils.findField(ExwsStep.DescriptorImpl.class, "templates");
        templatesField.setAccessible(true);
        ReflectionUtils.setField(templatesField, descriptor, templates);
    }

    private static WorkflowJob createWorkflowJob() throws IOException {
        String script = String.format("" +
                        " def externalWorkspace = exwsAllocate diskPoolId: '%s' \n" +
                        " node('linux') { \n" +
                        "   exws(externalWorkspace) { \n" +
                        "     sh \"echo 'foo' > bar.txt\" \n" +
                        "   } \n" +
                        " } ",
                DISK_POOL_ID);

        return createWorkflowJob(script);
    }

    private static WorkflowJob createDownstreamWorkflowJob(String upstreamJobName) throws IOException {
        String script = String.format("" +
                        " def run = selectRun job: '%s' \n" +
                        " def externalWorkspace = exwsAllocate selectedRun: run \n" +
                        " node('test') { \n" +
                        "   exws(externalWorkspace) { \n" +
                        "     sh \"cat bar.txt\"\n" +
                        "   } \n" +
                        " } ",
                upstreamJobName);

        return createWorkflowJob(script);
    }

    private static WorkflowJob createWorkflowJobWithTwoNodes() throws Exception {
        // The node labeled 'linux' writes random text to a file
        // Another node, labeled 'test', reads the file
        String script = format("" +
                        " def externalWorkspace = exwsAllocate diskPoolId: '%s' \n" +
                        " node('linux') { \n" +
                        "    exws(externalWorkspace) { \n" +
                        "        sh \"echo '%s' > bar.txt\"\n" +
                        "    } \n" +
                        " } \n" +
                        " node('test') { \n" +
                        "    exws(externalWorkspace) { \n" +
                        "       sh \"cat bar.txt\"\n" +
                        "    } \n" +
                        " } ",
                DISK_POOL_ID, TEXT);

        return createWorkflowJob(script);
    }

    private static WorkflowJob createWorkflowJob(String script) throws IOException {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(10));
        job.setDefinition(new CpsFlowDefinition(script));

        return job;
    }

    private WorkflowRun runWorkflowJob(WorkflowJob job) throws ExecutionException, InterruptedException {
        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0);
        assertThat(runFuture, notNullValue());
        run = runFuture.get();

        return run;
    }
}
