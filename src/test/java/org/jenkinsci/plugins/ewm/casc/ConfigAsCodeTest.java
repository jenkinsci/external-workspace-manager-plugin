package org.jenkinsci.plugins.ewm.casc;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.steps.ExwsAllocateStep;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.jvnet.hudson.test.JenkinsRule;
import io.jenkins.plugins.casc.ConfigurationAsCode;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
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
    public void should_support_configuration_as_code() throws Exception {
        String config = ConfigAsCodeTest.class.getResource("configuration-as-code.yaml").toString();
        ConfigurationAsCode.get().configure(config);

        // parse the yaml file and check if they match
        File file  = new File(ConfigAsCodeTest.class.getResource("configuration-as-code.yaml").getFile());
        String configContent = new String(Files.readAllBytes(file.toPath()));
        Yaml yaml = new Yaml();
        Map<String, Object> map = (Map<String, Object>) yaml.load(configContent);


        // get the jenkins rule object.
        Jenkins jenkins = r.getInstance();
        ExwsAllocateStep.DescriptorImpl descriptor = (ExwsAllocateStep.DescriptorImpl) jenkins.getDescriptor(ExwsAllocateStep.class);
        List<DiskPool> diskPoolList = descriptor.getDiskPools();
        DiskPool diskPool = diskPoolList.get(0);
        // assertion
        assertThat(diskPool.getDiskPoolId(), is("diskpool1"));
        assertThat(diskPool.getDisplayName(), is("diskpool1 display name"));
        assertThat(diskPool.getDisks().get(0).getDiskId(), is("disk1"));
        assertThat(diskPool.getDisks().get(0).getDisplayName(), is("disk one display name"));
        assertThat(diskPool.getDisks().get(0).getMasterMountPoint(), is("/tmp"));
    }

    @Test
    public void should_be_backward_compatible() throws Exception {
        String config = ConfigAsCodeTest.class.getResource("obsolete-configuration-as-code.yaml").toString();
        System.out.println("config: " + config);
        ConfigurationAsCode.get().configure(config);

        Jenkins jenkins = r.getInstance();
        ExwsAllocateStep.DescriptorImpl descriptor = (ExwsAllocateStep.DescriptorImpl) jenkins.getDescriptor(ExwsAllocateStep.class);
        List<DiskPool> diskPoolList = descriptor.getDiskPools();
        DiskPool diskPool = diskPoolList.get(0);

        assertThat(diskPool.getDiskPoolId(), is("diskpool1"));
        assertThat(diskPool.getDisplayName(), is("diskpool1 display name"));
        assertThat(diskPool.getDisks().get(0).getDiskId(), is("disk1"));
        assertThat(diskPool.getDisks().get(0).getDisplayName(), is("disk one display name"));
        assertThat(diskPool.getDisks().get(0).getMasterMountPoint(), is("/tmp"));
    }

    @Test
    public void export_configuration() throws Exception {
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
