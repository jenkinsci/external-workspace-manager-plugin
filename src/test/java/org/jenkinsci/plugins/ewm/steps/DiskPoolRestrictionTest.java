package org.jenkinsci.plugins.ewm.steps;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.job.RegexNameRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.job.StartedByMemberOfGroupRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.job.StartedByUserRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.GroupSelector;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.UserSelector;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Result;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;
import java.util.concurrent.Future;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.notNullValue;
import static org.jenkinsci.plugins.ewm.TestUtil.DISK_ID_ONE;
import static org.jenkinsci.plugins.ewm.TestUtil.DISK_POOL_ID;
import static org.jenkinsci.plugins.ewm.TestUtil.setUpDiskPools;
import static org.junit.Assert.assertThat;

/**
 * Tests for the Disk Pool restriction feature.
 *
 * @author Alexandru Somai
 */
public class DiskPoolRestrictionTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @ClassRule
    public static BuildWatcher watcher = new BuildWatcher();

    @After
    public void tearDown() {
        setUpDiskPools(j.jenkins, Collections.<DiskPool>emptyList());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    public void allowedUser() throws Exception {
        String userId = "foobar";
        UserSelector selector = new UserSelector(userId);
        JobRestriction restriction = new StartedByUserRestriction(singletonList(selector), false, false, false);
        setUpDiskPoolRestriction(restriction);

        authenticate(userId);
        WorkflowRun run = createWorkflowJobAndRun();

        j.assertBuildStatusSuccess(run);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), run);
    }

    @Test
    public void notAllowedUser() throws Exception {
        UserSelector selector = new UserSelector("userONE");
        JobRestriction restriction = new StartedByUserRestriction(singletonList(selector), false, false, false);
        setUpDiskPoolRestriction(restriction);

        authenticate("userTWO");
        WorkflowRun run = createWorkflowJobAndRun();

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains(format("Disk Pool identified by '%s' is not accessible due to the applied Disk Pool restriction: Started By User", DISK_POOL_ID), run);
    }

    @Test
    public void allowedAnonymousUser() throws Exception {
        JobRestriction restriction = new StartedByUserRestriction(Collections.<UserSelector>emptyList(), false, false, true);
        setUpDiskPoolRestriction(restriction);

        SecurityContextHolder.getContext().setAuthentication(null);
        WorkflowRun run = createWorkflowJobAndRun();

        j.assertBuildStatusSuccess(run);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), run);
    }

    @Test
    public void downstreamJobTriggerByRestrictedUser() throws Exception {
        String allowedUsername = "foo";
        UserSelector selector = new UserSelector(allowedUsername);
        JobRestriction restriction = new StartedByUserRestriction(singletonList(selector), false, false, false);
        setUpDiskPoolRestriction(restriction);

        authenticate(allowedUsername);
        WorkflowRun upstreamRun = createWorkflowJobAndRun();

        j.assertBuildStatusSuccess(upstreamRun);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), upstreamRun);

        String notAllowedUsername = "bar";
        authenticate(notAllowedUsername);

        WorkflowJob downstreamJob = j.jenkins.createProject(WorkflowJob.class, randomAlphanumeric(7));
        downstreamJob.setDefinition(new CpsFlowDefinition(format("" +
                "def run = runSelector '%s' \n" +
                "exwsAllocate selectedRun: run", upstreamRun.getParent().getFullName())));
        WorkflowRun downstreamRun = downstreamJob.scheduleBuild2(0, new CauseAction(new Cause.UserIdCause())).get();

        j.assertBuildStatus(Result.FAILURE, downstreamRun);
        j.assertLogContains(format("Disk Pool identified by '%s' is not accessible due to the applied Disk Pool restriction: Started By User", DISK_POOL_ID), downstreamRun);
    }

    @Test
    public void allowedGroup() throws Exception {
        String username = "foobar";
        String group = "allowed";

        GroupSelector groupSelector = new GroupSelector(group);
        JobRestriction restriction = new StartedByMemberOfGroupRestriction(singletonList(groupSelector), false);
        setUpDiskPoolRestriction(restriction);

        JenkinsRule.DummySecurityRealm securityRealm = j.createDummySecurityRealm();
        securityRealm.addGroups(username, group);
        j.jenkins.setSecurityRealm(securityRealm);

        authenticate(username);
        WorkflowRun run = createWorkflowJobAndRun();

        j.assertBuildStatusSuccess(run);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), run);
    }

    @Test
    public void notAllowedGroup() throws Exception {
        String username = "foobar";
        String group = "allowed";

        GroupSelector groupSelector = new GroupSelector("not-allowed-group");
        JobRestriction restriction = new StartedByMemberOfGroupRestriction(singletonList(groupSelector), false);
        setUpDiskPoolRestriction(restriction);

        JenkinsRule.DummySecurityRealm securityRealm = j.createDummySecurityRealm();
        securityRealm.addGroups(username, group);
        j.jenkins.setSecurityRealm(securityRealm);

        authenticate(username);
        WorkflowRun run = createWorkflowJobAndRun();

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains(format("Disk Pool identified by '%s' is not accessible due to the applied Disk Pool restriction: Started By member of group", DISK_POOL_ID), run);
    }

    @Test
    public void allowedJobRegexName() throws Exception {
        setUpDiskPoolRestriction(new RegexNameRestriction("test-.*", false));

        WorkflowRun run = createWorkflowJobAndRun("test-workflow");

        j.assertBuildStatusSuccess(run);
        j.assertLogContains(format("Selected Disk ID '%s' from the Disk Pool ID '%s'", DISK_ID_ONE, DISK_POOL_ID), run);
    }

    @Test
    public void notAllowedJobRegexName() throws Exception {
        setUpDiskPoolRestriction(new RegexNameRestriction("does not match", false));

        WorkflowRun run = createWorkflowJobAndRun("foobar");

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains(format("Disk Pool identified by '%s' is not accessible due to the applied Disk Pool restriction: Regular Expression (Job Name)", DISK_POOL_ID), run);
    }

    private static void setUpDiskPoolRestriction(JobRestriction restriction) {
        Disk disk = new Disk(DISK_ID_ONE, null, "any", null, null);
        DiskPool diskPool = new DiskPool(DISK_POOL_ID, null, null, null, restriction, null, singletonList(disk));
        setUpDiskPools(j.jenkins, singletonList(diskPool));
    }

    private static void authenticate(String principal) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "pass");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static WorkflowRun createWorkflowJobAndRun() throws Exception {
        return createWorkflowJobAndRun(randomAlphanumeric(7));
    }

    private static WorkflowRun createWorkflowJobAndRun(String jobName) throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, jobName);
        job.setDefinition(new CpsFlowDefinition(format("exwsAllocate diskPoolId: '%s'", DISK_POOL_ID)));
        Future<WorkflowRun> runFuture = job.scheduleBuild2(0, new CauseAction(new Cause.UserIdCause()));
        assertThat(runFuture, notNullValue());

        return runFuture.get();
    }
}
