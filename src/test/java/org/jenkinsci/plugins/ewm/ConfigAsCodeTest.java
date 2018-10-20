package org.jenkinsci.plugins.ewm;

import io.jenkins.plugins.casc.ConfigurationAsCode;

import org.jenkinsci.plugins.ewm.steps.ExwsAllocateStep;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

// TODO: the following should not be necessary since they should be tested in the job-restriction plugin in the end
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.job.RegexNameRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.job.StartedByUserRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.logic.AndJobRestriction;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Test for Configuration As Code Compatibility.
 *
 * @author Martin d'Anjou
 */
public class ConfigAsCodeTest {

    @ClassRule public static JenkinsRule j = new JenkinsRule();

    @Test
    public void should_support_configuration_as_code() throws Exception {
        String config = ConfigAsCodeTest.class.getResource("configuration-as-code.yaml").toString(); //configuration-as-code.yml").toString();
        System.out.println("config: " + config);
        ConfigurationAsCode.get().configure(config);
        ExwsAllocateStep.DescriptorImpl descriptor = (ExwsAllocateStep.DescriptorImpl) j.jenkins.getDescriptor(ExwsAllocateStep.class);
        assertThat(descriptor.diskPools.size(), is(1));
        assertThat(descriptor.diskPools.get(0).getDiskPoolId(), is("diskpool1"));
        assertThat(descriptor.diskPools.get(0).getDisplayName(), is("diskpool1 display name"));
        assertThat(descriptor.diskPools.get(0).getDescription(), is("diskpool1 description"));
        // TODO: find out why this next one does not work
        // assertThat(descriptor.diskPools.get(0).getWorkspaceTemplate(), is("${JOB_NAME}/${CUSTOM_BUILD_PARAM}/${BUILD_NUMBER}"));
        assertThat(descriptor.diskPools.get(0).getDisks().size(), is(1));
        assertThat(descriptor.diskPools.get(0).getDisks().get(0).getDiskId(), is("disk1"));
        assertThat(descriptor.diskPools.get(0).getDisks().get(0).getDisplayName(), is("disk one display name"));
        assertThat(descriptor.diskPools.get(0).getDisks().get(0).getMasterMountPoint(), is("/tmp"));
        assertThat(descriptor.diskPools.get(0).getDisks().get(0).getPhysicalPathOnDisk(), is("some/path"));
        assertNotNull(descriptor.diskPools.get(0).getDisks().get(0).getDiskInfo());
        assertThat(descriptor.diskPools.get(0).getDisks().get(0).getDiskInfo().getReadSpeed(), is (10));
        assertThat(descriptor.diskPools.get(0).getDisks().get(0).getDiskInfo().getWriteSpeed(), is (5));

        // TODO: These should only check that the restriction exists, the restriction details should be tested in the job-restriction-plugin itself
        assertNotNull(descriptor.diskPools.get(0).getRestriction());
        assertThat(descriptor.diskPools.get(0).getRestriction().getDescriptor().getDisplayName(), is("And"));
        assertTrue(descriptor.diskPools.get(0).getRestriction() instanceof AndJobRestriction);
        AndJobRestriction ajr = (AndJobRestriction)descriptor.diskPools.get(0).getRestriction();
        assertTrue(ajr.getFirst() instanceof RegexNameRestriction);
        assertTrue(ajr.getSecond() instanceof StartedByUserRestriction);
    }

}
