package org.jenkinsci.plugins.ewm.steps;

import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Result;
import org.jenkinsci.plugins.ewm.TestUtil;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.nodes.NodeDisk;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.filefilter.FileFilterUtils.directoryFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.nameFileFilter;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.jenkinsci.plugins.ewm.TestUtil.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for workspace cleanup feature.
 *
 * @author Alexandru Somai
 */
public class WorkspaceCleanupTest {

    private static final String PATH_ON_DISK = "path-on-disk";

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private static Node node1;
    private static Node node2;

    private File mountToDisk;

    @BeforeClass
    public static void setUp() throws Exception {
        node1 = j.createSlave(Label.get("linux"));
        node2 = j.createSlave(Label.get("test"));
        Disk disk = new Disk(DISK_ID_ONE, null, "mount-from-master", PATH_ON_DISK, null);
        DiskPool diskPool = new DiskPool(DISK_POOL_ID, null, null, null, null, null, Collections.singletonList(disk));
        setUpDiskPools(j.jenkins, diskPool);
    }

    @Before
    public void setUpFolder() throws IOException {
        mountToDisk = tmp.newFolder("mount-to-disk");
        NodeDisk nodeDisk = new NodeDisk(DISK_ID_ONE, mountToDisk.getPath());
        addExternalWorkspaceNodeProperty(node1, DISK_POOL_ID, nodeDisk);
        addExternalWorkspaceNodeProperty(node2, DISK_POOL_ID, nodeDisk);
    }

    @After
    public void resetNodeProperties() throws IOException {
        removeExternalWorkspaceNodeProperty(node1);
        removeExternalWorkspaceNodeProperty(node2);
    }

    @Test
    public void deleteWorkspace() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def extWorkspace = exwsAllocate diskPoolId: '%s' \n" +
                "node ('linux') { \n" +
                "	exws (extWorkspace) { \n" +
                "		try { \n" +
                "			writeFile file: 'foo.txt', text: 'bar' \n" +
                "		} finally { \n" +
                "			step ([$class: 'WsCleanup']) \n" +
                "		} \n" +
                "	} \n" +
                "}", DISK_POOL_ID));
        j.assertBuildStatusSuccess(run);
        j.assertLogContains("[WS-CLEANUP] Deleting project workspace...[WS-CLEANUP] done", run);
        assertThat(listFiles(tmp.getRoot(), nameFileFilter("foo.txt"), directoryFileFilter()), hasSize(0));
    }

    @Test
    public void keepWorkspaceIfBuildFails() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def extWorkspace = exwsAllocate diskPoolId: '%s' \n" +
                "node ('linux') { \n" +
                "   exws (extWorkspace) { \n" +
                "		try { \n" +
                "		    writeFile file: 'foo.txt', text: 'bar' \n" +
                "			throw new Exception() \n" +
                "		} catch (e) { \n" +
                "			currentBuild.result = 'FAILURE' \n" +
                "           throw e \n" +
                "		} finally { \n" +
                "			step ([$class: 'WsCleanup', cleanWhenFailure: false]) \n" +
                "		} \n" +
                "	} \n" +
                "}", DISK_POOL_ID));
        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("[WS-CLEANUP] Deleting project workspace...[WS-CLEANUP] Skipped based on build state FAILURE", run);

        Collection<File> files = listFiles(tmp.getRoot(), nameFileFilter("foo.txt"), directoryFileFilter());
        assertThat(files, hasSize(1));
        File workspaceFile = files.iterator().next();
        String expectedFilePath = Paths.get(mountToDisk.getAbsolutePath(),
                PATH_ON_DISK, run.getParent().getFullName(), Integer.toString(run.getNumber()), "foo.txt").toString();
        assertThat(workspaceFile.getAbsolutePath(), is(expectedFilePath));
    }

    @Test
    public void deleteWorkspaceBasedOnPattern() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def extWorkspace = exwsAllocate diskPoolId: '%s' \n" +
                "node ('linux') { \n" +
                "	exws (extWorkspace) { \n" +
                "		try { \n" +
                "		    writeFile file: 'foo.txt', text: 'foobar' \n" +
                "			writeFile file: 'bar.txt', text: 'foobar' \n" +
                "		} finally { \n" +
                "			step ([$class: 'WsCleanup', patterns: [[pattern: 'bar.*', type: 'INCLUDE']]]) \n" +
                "		} \n" +
                "	} \n" +
                "}", DISK_POOL_ID));
        j.assertBuildStatusSuccess(run);
        j.assertLogContains("[WS-CLEANUP] Deleting project workspace...[WS-CLEANUP] done", run);

        File wsDirectory = new File(mountToDisk, PATH_ON_DISK + File.separator + run.getParent().getFullName() + File.separator + run.getNumber());
        File[] wsFiles = wsDirectory.listFiles();
        assertThat(wsFiles, notNullValue());
        assertThat(wsFiles, arrayWithSize(1));
        assertThat(wsFiles[0].getName(), is("foo.txt"));
    }

    @Test
    public void deleteWorkspaceInTheDownstreamJob() throws Exception {
        WorkflowRun upstreamRun = createWorkflowJobAndRun(format("" +
                "def extWorkspace = exwsAllocate diskPoolId: '%s' \n" +
                "node ('linux') { \n" +
                "	exws (extWorkspace) { \n" +
                "		writeFile file: 'foo.txt', text: 'foobar' \n" +
                "	} \n" +
                "}", DISK_POOL_ID));
        j.assertBuildStatusSuccess(upstreamRun);
        assertThat(listFiles(tmp.getRoot(), nameFileFilter("foo.txt"), directoryFileFilter()), hasSize(1));

        WorkflowRun downstreamRun = createWorkflowJobAndRun(format("" +
                "def run = selectRun '%s' \n" +
                "def extWorkspace = exwsAllocate selectedRun: run \n" +
                "node ('test') { \n" +
                "	exws (extWorkspace) { \n" +
                "		try { \n" +
                "			def text = readFile file: 'foo.txt' \n" +
                "			echo text \n" +
                "		} finally { \n" +
                "			step ([$class: 'WsCleanup']) \n" +
                "		} \n" +
                "	} \n" +
                "}", upstreamRun.getParent().getFullName()));
        j.assertBuildStatusSuccess(downstreamRun);
        j.assertLogContains("foobar", downstreamRun);
        j.assertLogContains("[WS-CLEANUP] Deleting project workspace...[WS-CLEANUP] done", downstreamRun);
        assertThat(listFiles(tmp.getRoot(), nameFileFilter("foo.txt"), directoryFileFilter()), hasSize(0));
    }

    private static WorkflowRun createWorkflowJobAndRun(String script) throws Exception {
        return TestUtil.createWorkflowJobAndRun(j.jenkins, script);
    }
}
