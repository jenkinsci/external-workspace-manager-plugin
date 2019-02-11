package org.jenkinsci.plugins.ewm.casc;

import hudson.ExtensionList;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.steps.ExwsAllocateStep;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;

import org.jvnet.hudson.test.JenkinsRule;
import io.jenkins.plugins.casc.ConfigurationAsCode;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Test for Configuration As Code Compatibility.
 *
 * @author Martin d'Anjou
 */
public class ConfigAsCodeTest {

    @ClassRule public static JenkinsRule r = new JenkinsRule();

    @Test
    public void shouldSupportConfigurationAsCode() throws Exception {
        URL resource = ConfigAsCodeTest.class.getResource("configuration-as-code.yaml");
        String config = resource.toString();
        ConfigurationAsCode.get().configure(config);
        // get the jenkins rule object.

        ExwsAllocateStep.DescriptorImpl descriptor =  ExtensionList.lookupSingleton(ExwsAllocateStep.DescriptorImpl.class);
        List<DiskPool> diskPools = descriptor.getDiskPools();
        DiskPool diskPool = diskPools.get(0);

        // assertion
        assertThat(diskPool.getDiskPoolId(), is("diskpool1"));
        assertThat(diskPool.getDisplayName(), is("diskpool1 display name"));
        assertThat(diskPool.getDisks().get(0).getDiskId(), is("disk1"));
        assertThat(diskPool.getDisks().get(0).getDisplayName(), is("disk one display name"));
        assertThat(diskPool.getDisks().get(0).getMasterMountPoint(), is("/tmp"));
    }

    @Test
    public void exportConfiguration() throws Exception {
        Yaml yaml = new Yaml();

        // get the CasC configure
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        ConfigurationAsCode.get().export(outstream);
        ByteArrayInputStream instream = new ByteArrayInputStream(outstream.toByteArray());
        Map<String, Object> exportMap = (Map<String, Object>) yaml.load(instream);

        // get the yaml configure
        File file  = new File(ConfigAsCodeTest.class.getResource("configuration-as-code.yaml").getFile());
        FileInputStream fileInputStream = new FileInputStream(file);
        Map<String, Object> yamlMap = (Map<String, Object>) yaml.load(fileInputStream);

        // assert
        assertEquals(yamlMap.get("unclassified"), exportMap.get("unclassified"));
    }
}
