package org.jenkinsci.plugins.ewm.steps;

import hudson.model.Node;
import hudson.slaves.DumbSlave;
import hudson.slaves.RetentionStrategy;
import org.jenkinsci.plugins.ewm.definitions.Disk;
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
import java.util.ArrayList;
import java.util.List;

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

    @Rule
    public JenkinsRule j = new JenkinsRule();
    @Rule
    public TemporaryFolder tmp1 = new TemporaryFolder();
    @Rule
    public TemporaryFolder tmp2 = new TemporaryFolder();

    private WorkflowRun run;

    private File pathToDisk1;
    private File pathToDisk2;

    @Before
    public void setUpFolder() throws IOException {
        pathToDisk1 = tmp1.newFolder("mount-to-disk-one");
        pathToDisk2 = tmp2.newFolder("mount-to-disk-two");
    }

    @Test
    public void sharedWorkspaceForTwoDifferentNodes() throws Exception {
        String physicalPathOnDisk = "jenkins-project/disk-one";
        Disk disk1 = new Disk(DISK_ID_ONE, "name", pathToDisk1.getPath(), physicalPathOnDisk);
        Disk disk2 = new Disk(DISK_ID_TWO, "name", pathToDisk2.getPath(), physicalPathOnDisk);
        setupDiskPool(j.jenkins, DISK_POOL_ID, disk1, disk2);

        setupNodes();

        String text = "Write random text to a file";

        // On node labeled 'linux' write random text to a file
        // On the second node, labeled 'test', read the text from file
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

        String jobName = "job";
        run = createWorkflowJobAndRun(j.jenkins, jobName, script);

        Disk allocatedDisk = findAllocatedDisk(disk1, disk2);

        j.assertBuildStatusSuccess(run);
        j.assertLogContains(format("Running in %s/%s/%s/%d", allocatedDisk.getMasterMountPoint(), physicalPathOnDisk, jobName, run.getNumber()), run);
        // The text written to file should be printed twice on the console output (when writing and when reading it)
        assertThat(countMatches(JenkinsRule.getLog(run), text), is(2));
    }

    private void setupNodes() throws Exception {
        List<ExternalWorkspaceProperty> nodeProperties = new ArrayList<>();

        List<DiskNode> diskNodes = new ArrayList<>();
        diskNodes.add(new DiskNode(DISK_ID_ONE, pathToDisk1.getPath()));
        diskNodes.add(new DiskNode(DISK_ID_TWO, pathToDisk2.getPath()));
        nodeProperties.add(new ExternalWorkspaceProperty(DISK_POOL_ID, diskNodes));

        j.jenkins.addNode(new DumbSlave("node-one", "desc", "node-one-path-not-used", "1",
                Node.Mode.NORMAL, "linux", j.createComputerLauncher(null), RetentionStrategy.NOOP, nodeProperties));

        j.jenkins.addNode(new DumbSlave("node-two", "desc", "node-two-path-not-used", "1",
                Node.Mode.NORMAL, "test", j.createComputerLauncher(null), RetentionStrategy.NOOP, nodeProperties));
    }
}
