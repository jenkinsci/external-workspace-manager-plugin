package org.jenkinsci.plugins.ewm.casc;

import hudson.ExtensionList;
import hudson.model.Computer;
import hudson.model.Node;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.ewm.nodes.ExternalWorkspaceProperty;
import org.jenkinsci.plugins.ewm.nodes.NodeDiskPool;
import org.jenkinsci.plugins.ewm.steps.ExwsAllocateStep;
import org.jenkinsci.plugins.ewm.steps.ExwsStep;
import org.jenkinsci.plugins.ewm.definitions.Template;
import org.jenkinsci.plugins.ewm.nodes.NodeDisk;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;

import org.jvnet.hudson.test.JenkinsRule;
import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.snakeyaml.Yaml;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;

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
    public void shouldSupportConfigurationAsCodeExwsAllocateStep() throws Exception {
        String config = resource.toString();
        ConfigurationAsCode.get().configure(config);

        // Test ExwsAllocateStep
        ExwsAllocateStep.DescriptorImpl descriptor =  ExtensionList.lookupSingleton(ExwsAllocateStep.DescriptorImpl.class);
        List<DiskPool> diskPools = descriptor.getDiskPools();
        DiskPool diskPool = diskPools.get(0);

        assertThat(diskPool.getDiskPoolId(), is("diskpool1"));
        assertThat(diskPool.getDisplayName(), is("diskpool1 display name"));
        assertThat(diskPool.getDisks().get(0).getDiskId(), is("disk1"));
        assertThat(diskPool.getDisks().get(0).getDisplayName(), is("disk one display name"));
        assertThat(diskPool.getDisks().get(0).getMasterMountPoint(), is("/tmp"));
    }


    @Test
    public void shouldSupportConfigurationAsCodeExwsStep() throws Exception {
        String config = resource.toString();
        ConfigurationAsCode.get().configure(config);

        // Test ExwsStep
        ExwsStep.DescriptorImpl globalTemplateDescriptor = ExtensionList.lookupSingleton(ExwsStep.DescriptorImpl.class);
        List<Template> templates = globalTemplateDescriptor.getTemplates();

        assertThat(templates.get(0).getLabel(), is("all"));
        assertThat(templates.get(0).getNodeDiskPools().get(0).getDiskPoolRefId(), is("dp1"));
        NodeDisk nodeDisk = templates.get(0).getNodeDiskPools().get(0).getNodeDisks().get(0);
        assertThat(nodeDisk.getDiskRefId(), is("dp1refid1"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template11"));
        nodeDisk = templates.get(0).getNodeDiskPools().get(0).getNodeDisks().get(1);
        assertThat(nodeDisk.getDiskRefId(), is("dp1refid2"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template12"));
        assertThat(templates.get(0).getNodeDiskPools().get(1).getDiskPoolRefId(), is("dp2"));
        nodeDisk = templates.get(0).getNodeDiskPools().get(1).getNodeDisks().get(0);
        assertThat(nodeDisk.getDiskRefId(), is("dp2refid1"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template21"));
    }

    @Test
    public void shouldSupportConfigurationAsCodeMasterProperty() throws Exception {
        String config = resource.toString();
        ConfigurationAsCode.get().configure(config);

        Computer computer = r.getInstance().getComputer("");
        Node node = computer.getNode();
        List<NodeDiskPool> nodeDiskPools = node.getNodeProperties().get(ExternalWorkspaceProperty.class).getNodeDiskPools();
        assertThat(nodeDiskPools.get(0).getDiskPoolRefId(), is("master-node-id"));
        assertThat(nodeDiskPools.get(0).getNodeDisks().get(0).getDiskRefId(), is("master-node-disk"));
        assertThat(nodeDiskPools.get(0).getNodeDisks().get(0).getNodeMountPoint(), is("/tmp/master-node"));
    }

    @Test
    public void shouldSupportConfigurationAsCodeAgentProperty() throws Exception {
        String config = resource.toString();
        ConfigurationAsCode.get().configure(config);

        Computer computer = r.getInstance().getComputer("");
        Node node = computer.getNode();
        List<NodeDiskPool> nodeDiskPools = node.getNodeProperties().get(ExternalWorkspaceProperty.class).getNodeDiskPools();
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

        // test exwsAllocationStep and exwsStep
        Map<String, Object >unclassified = (Map<String, Object >) yamlMap.get("unclassified");
        Map<String, Object >unclassifiedExport = (Map<String, Object >) exportMap.get("unclassified");

        assertEquals(unclassified.get("exwsGlobalConfigurationDiskPools"), unclassifiedExport.get("exwsGlobalConfigurationDiskPools"));
        assertEquals(unclassified.get("exwsGlobalConfigurationTemplates"), unclassifiedExport.get("exwsGlobalConfigurationTemplates"));

        // test node, not available now
    }
}
