package org.jenkinsci.plugins.ewm.steps;

import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import org.apache.commons.lang.RandomStringUtils;
import org.jenkinsci.plugins.ewm.TestUtil;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.nodes.NodeDisk;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static java.lang.String.format;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.jenkinsci.plugins.ewm.TestUtil.*;
import static org.junit.Assert.assertThat;

/**
 * Tests the custom workspace path on disk feature.
 *
 * @author Alexandru Somai
 */
public class CustomWorkspaceTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @ClassRule
    public static BuildWatcher watcher = new BuildWatcher();

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File tmpFolder;

    @Before
    public void setUp() throws IOException {
        Disk disk = new Disk(DISK_ID_ONE, null, "mount", null, null);
        DiskPool diskPool = new DiskPool(DISK_POOL_ID, null, null, null, null, null, Collections.singletonList(disk));
        setUpDiskPools(j.jenkins, diskPool);

        tmpFolder = tmp.newFolder();
        NodeDisk nodeDisk = new NodeDisk(DISK_ID_ONE, tmpFolder.getAbsolutePath());
        addExternalWorkspaceNodeProperty(j.jenkins, DISK_POOL_ID, nodeDisk);
    }

    @After
    public void tearDown() throws IOException {
        removeExternalWorkspaceNodeProperty(j.jenkins);
        removeDiskPools(j.jenkins);
    }

    @Test
    public void customWorkspaceUsingPathParameter() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(7));
        job.setDefinition(new CpsFlowDefinition(format("" +
                "def customPath = \"${env.JOB_NAME}/${PR_BUILD_NUMBER}/${env.BUILD_NUMBER}\" \n" +
                "def extWorkspace = exwsAllocate diskPoolId: '%s', path: customPath \n" +
                "node { \n" +
                "   exws (extWorkspace) { \n" +
                "       writeFile file: 'foobar.txt', text: 'foobar' \n" +
                "   } \n" +
                "} \n", DISK_POOL_ID)));

        job.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("PR_BUILD_NUMBER", "")));

        String prBuildNumberValue = "22";
        ParameterValue parameterValue = new StringParameterValue("PR_BUILD_NUMBER", prBuildNumberValue);
        ParametersAction parameterValues = new ParametersAction(parameterValue);

        WorkflowRun run = j.assertBuildStatusSuccess(job.scheduleBuild2(0, parameterValues));
        verifyWorkspacePath(format("%s/%s/%d", job.getFullName(), prBuildNumberValue, run.getNumber()), run);
    }

    @Test
    public void customWorkspacePathParameterIsAbsolute() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def customPath = \"/${env.JOB_NAME}/${env.BUILD_NUMBER}\" \n" +
                "def extWorkspace = exwsAllocate diskPoolId: '%s', path: customPath", DISK_POOL_ID));

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains(format("ERROR: The custom path: /%s/%s must be a relative path", run.getParent().getFullName(), run.getNumber()), run);
    }

    @Test
    public void customWorkspaceUsingPathParameterWithConstantFolder() throws Exception {
        String folder = "constant";
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(7));
        job.setDefinition(new CpsFlowDefinition(format("" +
                "def customPath = \"%s/${env.JOB_NAME}/${env.BUILD_NUMBER}\" \n" +
                "def extWorkspace = exwsAllocate diskPoolId: '%s', path: customPath \n" +
                "node { \n" +
                "   exws (extWorkspace) { \n" +
                "       writeFile file: 'foobar.txt', text: 'foobar' \n" +
                "   } \n" +
                "} \n", folder, DISK_POOL_ID)));

        WorkflowRun run = j.assertBuildStatusSuccess(job.scheduleBuild2(0));
        verifyWorkspacePath(format("%s/%s/%d", folder, job.getFullName(), run.getNumber()), run);
    }

    @Test
    public void wrongStringInterpolation() throws Exception {
        // the script uses single quotes, instead of double quotes for String interpolation.
        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def customPath = '${env.JOB_NAME}/${env.BUILD_NUMBER}' \n" +
                "def extWorkspace = exwsAllocate diskPoolId: '%s', path: customPath", DISK_POOL_ID));

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("The custom path: ${env.JOB_NAME}/${env.BUILD_NUMBER} contains '${' characters. Did you resolve correctly the parameters with Build DSL?", run);
    }

    @Test
    public void globalWorkspaceTemplate() throws Exception {
        setGlobalWorkspaceTemplate("${JOB_NAME}/${PR_BUILD_NUMBER}/${BUILD_NUMBER}");

        String prBuildNumberValue = "30";
        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def extWorkspace = null \n" +
                "withEnv (['PR_BUILD_NUMBER=%s']) { \n" +
                "   extWorkspace = exwsAllocate diskPoolId: '%s' \n" +
                "} \n" +
                "node { \n" +
                "   exws (extWorkspace) { \n" +
                "       writeFile file: 'foobar.txt', text: 'foobar' \n" +
                "   } \n" +
                "} \n", prBuildNumberValue, DISK_POOL_ID));

        j.assertBuildStatusSuccess(run);
        verifyWorkspacePath(format("%s/%s/%d", run.getParent().getFullName(), prBuildNumberValue, run.getNumber()), run);
    }

    @Test
    public void globalWorkspaceTemplateWithConstantFolder() throws Exception {
        String folder = "constant";
        setGlobalWorkspaceTemplate(format("%s/${JOB_NAME}/${BUILD_NUMBER}", folder));

        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def extWorkspace = exwsAllocate diskPoolId: '%s' \n" +
                "node { \n" +
                "   exws (extWorkspace) { \n" +
                "       writeFile file: 'foobar.txt', text: 'foobar' \n" +
                "   } \n" +
                "} \n", DISK_POOL_ID));

        j.assertBuildStatusSuccess(run);
        verifyWorkspacePath(format("%s/%s/%d", folder, run.getParent().getFullName(), run.getNumber()), run);
    }

    @Test
    public void globalWorkspaceTemplatePathIsAbsolute() throws Exception {
        setGlobalWorkspaceTemplate("/${JOB_NAME}/${BUILD_NUMBER}");

        WorkflowRun run = createWorkflowJobAndRun(format("def extWorkspace = exwsAllocate diskPoolId: '%s'", DISK_POOL_ID));

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains(format("ERROR: Workspace template defined for Disk Pool '%s' must be a relative path", DISK_POOL_ID), run);
    }

    @Test
    public void globalWorkspaceTemplateWithTypo() throws Exception {
        setGlobalWorkspaceTemplate("${JOB_NAME_WITH_TYPO}/${BUILD_NUMBER}");

        WorkflowRun run = createWorkflowJobAndRun(format("def extWorkspace = exwsAllocate diskPoolId: '%s'", DISK_POOL_ID));

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("ERROR: Can't resolve the following workspace template: ${JOB_NAME_WITH_TYPO}/${BUILD_NUMBER}. " +
                "The resulting path is: ${JOB_NAME_WITH_TYPO}/1. Did you provide all the needed environment variables?", run);
    }

    private static void setGlobalWorkspaceTemplate(String template) {
        Disk disk = new Disk(DISK_ID_ONE, null, "mount", null, null);
        DiskPool diskPool = new DiskPool(DISK_POOL_ID, null, null, template, null, null, Collections.singletonList(disk));
        setUpDiskPools(j.jenkins, diskPool);
    }

    private void verifyWorkspacePath(String computedCustomPath, WorkflowRun run) throws Exception {
        File customWorkspace = new File(tmpFolder, computedCustomPath);
        File[] files = customWorkspace.listFiles();

        j.assertLogContains(format("The path on Disk is: %s", computedCustomPath), run);
        j.assertLogContains(format("Running in %s/%s", tmpFolder.getAbsolutePath(), computedCustomPath), run);
        assertThat(files, notNullValue());
        assertThat(files, arrayWithSize(1));
        assertThat(files[0].getName(), is("foobar.txt"));
    }

    private WorkflowRun createWorkflowJobAndRun(String script) throws Exception {
        return TestUtil.createWorkflowJobAndRun(j.jenkins, script);
    }
}
