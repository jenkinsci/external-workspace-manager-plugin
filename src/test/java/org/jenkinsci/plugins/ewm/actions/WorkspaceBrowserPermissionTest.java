package org.jenkinsci.plugins.ewm.actions;

import com.gargoylesoftware.htmlunit.Page;
import hudson.model.Item;
import hudson.model.Node;
import hudson.model.queue.QueueTaskFuture;
import jenkins.model.Jenkins;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.model.ExternalWorkspace;
import org.jenkinsci.plugins.ewm.nodes.NodeDisk;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;

import java.io.File;
import java.util.Collections;

import static org.jenkinsci.plugins.ewm.TestUtil.DISK_ID_ONE;
import static org.jenkinsci.plugins.ewm.TestUtil.DISK_POOL_ID;
import static org.jenkinsci.plugins.ewm.TestUtil.addExternalWorkspaceNodeProperty;
import static org.jenkinsci.plugins.ewm.TestUtil.setUpDiskPools;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Security tests for {@link WorkspaceBrowserRootAction}, ensuring that browsing an external workspace through the global
 * {@code /exws/browse/{workspaceId}/ws/} route requires read access to a job associated with that workspace.
 *
 * @author Alexandru Somai
 */
public class WorkspaceBrowserPermissionTest {

    private static final String CANARY_CONTENT = "SECRET_CANARY_CONTENT";
    private static final String CANARY_FILE = "canary.txt";

    private static final String ADMIN = "admin";
    private static final String READER = "reader";
    private static final String DEVELOPER = "developer";

    @Rule
    public JenkinsRule j = new JenkinsRule();
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private WorkflowJob job;
    private String workspaceId;

    @Before
    public void setUp() throws Exception {
        Node node = j.createSlave("node-one", "linux", null);

        File pathToDisk = tmp.newFolder("mount-to-disk-one");
        Disk disk = new Disk(DISK_ID_ONE, "name one", pathToDisk.getPath(),
                FilenameUtils.separatorsToSystem("jenkins-project/disk-one"), null);
        DiskPool diskPool = new DiskPool(DISK_POOL_ID, "name", "desc", null, null, null, Collections.singletonList(disk));
        setUpDiskPools(j.jenkins, diskPool);
        addExternalWorkspaceNodeProperty(node, DISK_POOL_ID, new NodeDisk(DISK_ID_ONE, pathToDisk.getPath()));

        // Allocate a workspace and write a canary file into it. The build runs as SYSTEM, independent of the web
        // authorization set up below.
        job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(10));
        job.setDefinition(new CpsFlowDefinition(String.format("" +
                " def externalWorkspace = exwsAllocate diskPoolId: '%s' \n" +
                " node('linux') { \n" +
                "   exws(externalWorkspace) { \n" +
                "       writeFile file: '%s', text: '%s'\n" +
                "   } \n" +
                " } ", DISK_POOL_ID, CANARY_FILE, CANARY_CONTENT)));

        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0);
        assertThat(runFuture, notNullValue());
        WorkflowRun run = runFuture.get();
        j.assertBuildStatusSuccess(run);

        ExwsAllocateActionImpl action = run.getAction(ExwsAllocateActionImpl.class);
        assertThat(action, notNullValue());
        ExternalWorkspace workspace = action.getAllocatedWorkspaces().get(0);
        workspaceId = workspace.getId();

        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy authorizationStrategy = new MockAuthorizationStrategy();
        authorizationStrategy.grant(Jenkins.ADMINISTER).everywhere().to(ADMIN);
        // The reader has Overall/Read only, but no Item/Read on the job that owns the workspace.
        authorizationStrategy.grant(Jenkins.READ).everywhere().to(READER);
        // The developer additionally has Item/Read on the job that owns the workspace.
        authorizationStrategy.grant(Jenkins.READ).everywhere().to(DEVELOPER);
        authorizationStrategy.grant(Item.READ).onItems(job).to(DEVELOPER);
        j.jenkins.setAuthorizationStrategy(authorizationStrategy);
    }

    @Test
    public void overallReadUserWithoutItemReadCannotBrowseWorkspace() throws Exception {
        JenkinsRule.WebClient wc = j.createWebClient()
                .withThrowExceptionOnFailingStatusCode(false)
                .login(READER);

        Page page = wc.goTo(browseUrl(), null);

        assertThat(page.getWebResponse().getStatusCode(), is(404));
        assertThat(page.getWebResponse().getContentAsString(), not(containsString(CANARY_CONTENT)));
    }

    @Test
    public void userWithItemReadCanBrowseWorkspace() throws Exception {
        JenkinsRule.WebClient wc = j.createWebClient().login(DEVELOPER);

        Page page = wc.goTo(browseUrl(), null);

        assertThat(page.getWebResponse().getStatusCode(), is(200));
        assertThat(page.getWebResponse().getContentAsString(), containsString(CANARY_CONTENT));
    }

    @Test
    public void adminCanBrowseWorkspace() throws Exception {
        JenkinsRule.WebClient wc = j.createWebClient().login(ADMIN);

        Page page = wc.goTo(browseUrl(), null);

        assertThat(page.getWebResponse().getStatusCode(), is(200));
        assertThat(page.getWebResponse().getContentAsString(), containsString(CANARY_CONTENT));
    }

    private String browseUrl() {
        return String.format("exws/browse/%s/ws/%s", workspaceId, CANARY_FILE);
    }
}
