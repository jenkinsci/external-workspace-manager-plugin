package org.jenkinsci.plugins.ewm.steps;

import hudson.model.Node;
import hudson.model.queue.QueueTaskFuture;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import hudson.util.ReflectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.definitions.Template;
import org.jenkinsci.plugins.ewm.nodes.DiskNode;
import org.jenkinsci.plugins.ewm.nodes.ExternalWorkspaceProperty;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static hudson.model.Result.FAILURE;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.countMatches;
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
    private static DiskNode diskNode1;
    private static DiskNode diskNode2;

    private static WorkflowJob job;

    private WorkflowRun run;

    @BeforeClass
    public static void setUp() throws Exception {
        node1 = j.createSlave("node-one", "linux", null);
        node2 = j.createSlave("node-two", "test", null);

        File pathToDisk1 = tmp1.newFolder("mount-to-disk-one");
        File pathToDisk2 = tmp2.newFolder("mount-to-disk-two");

        disk1 = new Disk(DISK_ID_ONE, "name one", pathToDisk1.getPath(), "jenkins-project/disk-one");
        disk2 = new Disk(DISK_ID_TWO, "name two", pathToDisk2.getPath(), "jenkins-project/disk-two");
        DiskPool diskPool = new DiskPool(DISK_POOL_ID, "name", "desc", Arrays.asList(disk1, disk2));
        setUpDiskPools(j.jenkins, Collections.singletonList(diskPool));

        diskNode1 = new DiskNode(DISK_ID_ONE, pathToDisk1.getPath());
        diskNode2 = new DiskNode(DISK_ID_TWO, pathToDisk2.getPath());

        job = createWorkflowJob();
    }

    @After
    public void tearDown() {
        resetTemplates();
        resetExternalWorkspaceNodeProperty(node1);
        resetExternalWorkspaceNodeProperty(node2);
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
        setUpTemplates(new Template("", "linux", Collections.<DiskNode>emptyList()));

        runWorkflowJob(job);

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains("In Jenkins global config, the Template labeled 'linux' does not have defined a Disk Pool Ref ID", run);
    }

    @Test
    public void wrongDiskPoolRefIdInTemplate() throws Exception {
        String wrongDiskPoolId = "random";
        setUpTemplates(new Template(wrongDiskPoolId, "linux", Collections.<DiskNode>emptyList()));

        runWorkflowJob(job);

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(format("In Jenkins global config, the Template labeled 'linux' has defined a wrong Disk Pool Ref ID '%s'." +
                " The correct Disk Pool Ref ID should be '%s', as the one used by the exwsAllocate step", wrongDiskPoolId, DISK_POOL_ID), run);
    }

    @Test
    public void missingNodeExternalWorkspaceProperty() throws Exception {
        runWorkflowJob(job);

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains("There is no External Workspace config defined in Node 'node-one' config", run);
    }

    @Test
    public void missingDiskPoolRefIdFromNodeProperty() throws Exception {
        setExternalWorkspaceNodeProperty(node1, "", diskNode1, diskNode2);

        runWorkflowJob(job);

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains("The Disk Pool Ref ID was not provided in Node 'node-one' config", run);
    }

    @Test
    public void wrongDiskPoolRefIdInNodeProperty() throws Exception {
        String wrongDiskPoolId = "random";
        setExternalWorkspaceNodeProperty(node1, wrongDiskPoolId, diskNode1, diskNode2);

        runWorkflowJob(job);

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(format("In Node 'node-one' config, the defined Disk Pool Ref ID '%s' does not match the one allocated by the exwsAllocate step '%s'", wrongDiskPoolId, DISK_POOL_ID), run);
    }

    @Test
    public void missingDiskRefIdFromNodeProperty() throws Exception {
        setExternalWorkspaceNodeProperty(node1, DISK_POOL_ID, new DiskNode("", "local-path"));

        runWorkflowJob(job);

        Disk selectedDisk = findAllocatedDisk(disk1, disk2);
        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(format("The Node 'node-one' config does not have defined any Disk Ref ID '%s'", selectedDisk.getDiskId()), run);
    }

    @Test
    public void missingLocalRootPathFromNodeProperty() throws Exception {
        setExternalWorkspaceNodeProperty(node1, DISK_POOL_ID, new DiskNode(DISK_ID_ONE, ""));

        runWorkflowJob(job);

        Disk selectedDisk = findAllocatedDisk(disk1, disk2);
        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(format("The Node 'node-one' config does not have defined any local root path for Disk Ref ID '%s'", selectedDisk.getDiskId()), run);
    }

    @Test
    public void sharedWorkspaceBetweenTwoDifferentNodes() throws Exception {
        setExternalWorkspaceNodeProperty(node1, DISK_POOL_ID, diskNode1, diskNode2);
        setExternalWorkspaceNodeProperty(node2, DISK_POOL_ID, diskNode1, diskNode2);

        WorkflowJob jobWithTwoNodes = createWorkflowJobWithTwoNodes();
        runWorkflowJob(jobWithTwoNodes);
        Disk allocatedDisk = findAllocatedDisk(disk1, disk2);

        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Searching for disk definitions in the External Workspace Templates from Jenkins global config", run);
        j.assertLogContains("Searching for disk definitions in the Node config", run);
        j.assertLogContains(format("Running in %s/%s/%s/%d",
                allocatedDisk.getMasterMountPoint(), allocatedDisk.getPhysicalPathOnDisk(), run.getParent().getName(), run.getNumber()), run);
        // The text written to file should be printed twice on the console output (when writing and when reading the file)
        assertThat(countMatches(JenkinsRule.getLog(run), TEXT), is(2));
    }

    @Test
    public void sharedWorkspaceBetweenTwoDifferentNodesWithTemplate() throws Exception {
        Template linuxTemplate = new Template(DISK_POOL_ID, "linux", Arrays.asList(diskNode1, diskNode2));
        Template testTemplate = new Template(DISK_POOL_ID, "test", Arrays.asList(diskNode1, diskNode2));
        setUpTemplates(linuxTemplate, testTemplate);

        WorkflowJob jobWithTwoNodes = createWorkflowJobWithTwoNodes();
        runWorkflowJob(jobWithTwoNodes);
        Disk allocatedDisk = findAllocatedDisk(disk1, disk2);

        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Searching for disk definitions in the External Workspace Templates from Jenkins global config", run);
        j.assertLogNotContains("Searching for disk definitions in the Node config", run);
        j.assertLogContains(format("Running in %s/%s/%s/%d",
                allocatedDisk.getMasterMountPoint(), allocatedDisk.getPhysicalPathOnDisk(), run.getParent().getName(), run.getNumber()), run);
        // The text written to file should be printed twice on the console output (when writing and when reading the file)
        assertThat(countMatches(JenkinsRule.getLog(run), TEXT), is(2));
    }

    @Test
    public void twoDifferentJobsUsingTheSameWorkspace() throws Exception {
        setExternalWorkspaceNodeProperty(node1, DISK_POOL_ID, diskNode1, diskNode2);
        setExternalWorkspaceNodeProperty(node2, DISK_POOL_ID, diskNode1, diskNode2);

        WorkflowJob upstreamJob = createWorkflowJob();
        WorkflowRun upstreamRun = runWorkflowJob(upstreamJob);
        Disk allocatedDisk = findAllocatedDisk(disk1, disk2);

        WorkflowJob downstreamJob = createDownstreamWorkflowJob(upstreamJob.getFullName());
        WorkflowRun downstreamRun = runWorkflowJob(downstreamJob);

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertBuildStatusSuccess(downstreamRun);
        j.assertLogContains("Searching for disk definitions in the External Workspace Templates from Jenkins global config", downstreamRun);
        j.assertLogContains("Searching for disk definitions in the Node config", downstreamRun);
        j.assertLogContains(format("Running in %s/%s/%s/%d",
                allocatedDisk.getMasterMountPoint(), allocatedDisk.getPhysicalPathOnDisk(), upstreamJob.getFullDisplayName(), upstreamRun.getNumber()), downstreamRun);
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

    private static void setExternalWorkspaceNodeProperty(Node node, String diskPoolRefId, DiskNode... diskNodes) {
        node.getNodeProperties().add(new ExternalWorkspaceProperty(diskPoolRefId, Arrays.asList(diskNodes)));
    }

    private static void resetExternalWorkspaceNodeProperty(Node node) {
        DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties = node.getNodeProperties();
        Iterator<NodeProperty<?>> nodePropertyIterator = nodeProperties.iterator();
        while (nodePropertyIterator.hasNext()) {
            NodeProperty<?> nodeProperty = nodePropertyIterator.next();
            if (nodeProperty instanceof ExternalWorkspaceProperty) {
                nodePropertyIterator.remove();
            }
        }
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
                        " def externalWorkspace = exwsAllocate upstream: '%s' \n" +
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
        job.setDefinition(new CpsFlowDefinition(script, true));

        return job;
    }

    private WorkflowRun runWorkflowJob(WorkflowJob job) throws ExecutionException, InterruptedException {
        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0);
        assertThat(runFuture, notNullValue());
        run = runFuture.get();

        return run;
    }
}
