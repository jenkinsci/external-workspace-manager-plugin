package org.jenkinsci.plugins.ewm.steps;

import hudson.model.Node;
import hudson.slaves.DumbSlave;
import hudson.slaves.RetentionStrategy;
import hudson.util.ReflectionUtils;
import org.jenkinsci.plugins.ewm.TestUtil;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.Template;
import org.jenkinsci.plugins.ewm.nodes.DiskNode;
import org.jenkinsci.plugins.ewm.nodes.ExternalWorkspaceProperty;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hudson.model.Result.FAILURE;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.hamcrest.Matchers.is;
import static org.jenkinsci.plugins.ewm.TestUtil.*;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link ExwsStep}.
 *
 * @author Alexandru Somai
 */
public class ExwsStepTest {

    private static final String JOB_NAME = "job-name";

    @Rule
    public JenkinsRule j = new JenkinsRule();
    @Rule
    public TemporaryFolder tmp1 = new TemporaryFolder();
    @Rule
    public TemporaryFolder tmp2 = new TemporaryFolder();

    private WorkflowRun run;

    private Disk disk1;
    private Disk disk2;
    private DiskNode diskNode1;
    private DiskNode diskNode2;

    @Before
    public void setUp() throws IOException {
        File pathToDisk1 = tmp1.newFolder("mount-to-disk-one");
        File pathToDisk2 = tmp2.newFolder("mount-to-disk-two");

        disk1 = new Disk(DISK_ID_ONE, "name", pathToDisk1.getPath(), "jenkins-project/disk-one");
        disk2 = new Disk(DISK_ID_TWO, "name", pathToDisk2.getPath(), "jenkins-project/disk-two");
        setUpDiskPool(j.jenkins, DISK_POOL_ID, disk1, disk2);

        diskNode1 = new DiskNode(DISK_ID_ONE, pathToDisk1.getPath());
        diskNode2 = new DiskNode(DISK_ID_TWO, pathToDisk2.getPath());
    }

    @Test
    public void missingExternalWorkspaceParam() throws Exception {
        setUpNodes();

        createWorkflowJobAndRun("" +
                " node('linux') { \n" +
                "   exws() { } \n" +
                " } ");

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains("No external workspace provided. Did you run the exwsAllocate step?", run);
    }

    @Test
    public void missingDiskPoolRefIdFromTemplate() throws Exception {
        setUpNodes();
        setUpTemplate("");

        createWorkflowJobAndRun();

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains("In Jenkins global config, the Template labeled 'linux' does not have defined a Disk Pool Ref ID", run);
    }

    @Test
    public void wrongDiskPoolRefIdInTemplate() throws Exception {
        setUpNodes();
        String wrongDiskPoolId = "random";
        setUpTemplate(wrongDiskPoolId);

        createWorkflowJobAndRun();

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(format("In Jenkins global config, the Template labeled 'linux' has defined a wrong Disk Pool Ref ID '%s'." +
                " The correct Disk Pool Ref ID should be '%s', as the one used by the exwsAllocate step", wrongDiskPoolId, DISK_POOL_ID), run);
    }

    @Test
    public void missingNodeExternalWorkspaceProperty() throws Exception {
        setUpNodes(DISK_POOL_ID);

        createWorkflowJobAndRun();

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains("There is no External Workspace config defined in Node 'node-one' config", run);
    }

    @Test
    public void missingDiskPoolRefIdFromNodeProperty() throws Exception {
        setUpNodes("", diskNode1, diskNode2);

        createWorkflowJobAndRun();

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains("The Disk Pool Ref ID was not provided in Node 'node-one' config", run);
    }

    @Test
    public void wrongDiskPoolRefIdInNodeProperty() throws Exception {
        String wrongDiskPoolId = "random";
        setUpNodes(wrongDiskPoolId, diskNode1, diskNode2);

        createWorkflowJobAndRun();

        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(format("In Node 'node-one' config, the defined Disk Pool Ref ID '%s' does not match the one allocated by the exwsAllocate step '%s'", wrongDiskPoolId, DISK_POOL_ID), run);
    }

    @Test
    public void missingDiskRefIdFromNodeProperty() throws Exception {
        setUpNodes(DISK_POOL_ID, new DiskNode("", "local-path"));

        createWorkflowJobAndRun();

        Disk selectedDisk = findAllocatedDisk(disk1, disk2);
        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(format("The Node 'node-one' config does not have defined any Disk Ref ID '%s'", selectedDisk.getDiskId()), run);
    }

    @Test
    public void missingLocalRootPathFromNodeProperty() throws Exception {
        setUpNodes(DISK_POOL_ID, new DiskNode(DISK_ID_ONE, ""));

        createWorkflowJobAndRun();

        Disk selectedDisk = findAllocatedDisk(disk1, disk2);
        j.assertBuildStatus(FAILURE, run);
        j.assertLogContains(format("The Node 'node-one' config does not have defined any local root path for Disk Ref ID '%s'", selectedDisk.getDiskId()), run);
    }

    @Test
    public void useDiskNodeDefinitionsFromTemplate() throws Exception {
        setUpNodes();
        setUpTemplate(diskNode1, diskNode2);

        createWorkflowJobAndRun();
        Disk allocatedDisk = findAllocatedDisk(disk1, disk2);

        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Searching for disk definitions in the External Workspace Templates from Jenkins global config", run);
        j.assertLogNotContains("Searching for disk definitions in the Node config", run);
        j.assertLogContains(format("Running in %s/%s/%s/%d",
                allocatedDisk.getMasterMountPoint(), allocatedDisk.getPhysicalPathOnDisk(), JOB_NAME, run.getNumber()), run);
    }

    @Test
    public void sharedWorkspaceBetweenTwoDifferentNodes() throws Exception {
        setUpNodes(diskNode1, diskNode2);

        // The node labeled 'linux' writes random text to a file
        // Another node, labeled 'test', reads the file
        String text = "Write random text to a file";
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
                DISK_POOL_ID, text);
        createWorkflowJobAndRun(script);
        Disk allocatedDisk = findAllocatedDisk(disk1, disk2);

        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Searching for disk definitions in the External Workspace Templates from Jenkins global config", run);
        j.assertLogContains("Searching for disk definitions in the Node config", run);
        j.assertLogContains(format("Running in %s/%s/%s/%d",
                allocatedDisk.getMasterMountPoint(), allocatedDisk.getPhysicalPathOnDisk(), JOB_NAME, run.getNumber()), run);
        // The text written to file should be printed twice on the console output (when writing and when reading the file)
        assertThat(countMatches(JenkinsRule.getLog(run), text), is(2));
    }

    private void setUpTemplate(DiskNode... diskNodes) {
        setUpTemplate(DISK_POOL_ID, diskNodes);
    }

    private void setUpTemplate(String diskPoolRefId, DiskNode... diskNodes) {
        ExwsStep.DescriptorImpl descriptor = (ExwsStep.DescriptorImpl) j.jenkins.getDescriptor(ExwsStep.class);

        List<Template> templates = new ArrayList<>();
        Template template = new Template(diskPoolRefId, "linux", Arrays.asList(diskNodes));
        templates.add(template);

        Field templatesField = ReflectionUtils.findField(ExwsStep.DescriptorImpl.class, "templates");
        templatesField.setAccessible(true);
        ReflectionUtils.setField(templatesField, descriptor, templates);
    }

    private void setUpNodes(DiskNode... diskNodes) throws Exception {
        setUpNodes(DISK_POOL_ID, diskNodes);
    }

    private void setUpNodes(String diskPoolRefId, DiskNode... diskNodes) throws Exception {
        List<ExternalWorkspaceProperty> nodeProperties = new ArrayList<>();
        if (diskNodes.length > 0) {
            nodeProperties.add(new ExternalWorkspaceProperty(diskPoolRefId, Arrays.asList(diskNodes)));
        }

        j.jenkins.addNode(new DumbSlave("node-one", "desc", "node-one-path-not-used", "1",
                Node.Mode.NORMAL, "linux", j.createComputerLauncher(null), RetentionStrategy.NOOP, nodeProperties));

        j.jenkins.addNode(new DumbSlave("node-two", "desc", "node-two-path-not-used", "1",
                Node.Mode.NORMAL, "test", j.createComputerLauncher(null), RetentionStrategy.NOOP, nodeProperties));
    }

    private void createWorkflowJobAndRun() throws Exception {
        createWorkflowJobAndRun(String.format("" +
                        " def externalWorkspace = exwsAllocate diskPoolId: '%s' \n" +
                        " node('linux') { \n" +
                        "   exws(externalWorkspace) { \n" +
                        "     sh \"echo 'foo' > bar.txt\" \n" +
                        "   } \n" +
                        " } ",
                DISK_POOL_ID));
    }

    private void createWorkflowJobAndRun(String script) throws Exception {
        run = TestUtil.createWorkflowJobAndRun(j.jenkins, JOB_NAME, script);
    }
}
