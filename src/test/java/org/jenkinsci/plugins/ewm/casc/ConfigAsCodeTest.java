package org.jenkinsci.plugins.ewm.casc;

import hudson.ExtensionList;
import hudson.model.Computer;
import hudson.model.Node;
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

import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import static io.jenkins.plugins.casc.misc.Util.getUnclassifiedRoot;
import static io.jenkins.plugins.casc.misc.Util.toStringFromYamlFile;
import static io.jenkins.plugins.casc.misc.Util.toYamlString;

import java.util.List;

/**
 * Test for Configuration As Code Compatibility.
 *
 * @author Martin d'Anjou
 */
public class ConfigAsCodeTest {

    @ClassRule
    @ConfiguredWithCode("configuration-as-code.yaml")
    public static JenkinsConfiguredWithCodeRule  j = new JenkinsConfiguredWithCodeRule();

    @Test
    public void shouldSupportConfigurationAsCodeExwsAllocateStep() throws Exception {
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
        // Test ExwsStepfirst
        ExwsStep.DescriptorImpl globalTemplateDescriptor = ExtensionList.lookupSingleton(ExwsStep.DescriptorImpl.class);
        List<Template> templates = globalTemplateDescriptor.getTemplates();
        // First template test
        assertThat(templates.get(0).getLabel(), is("first"));
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
        // Second template test
        assertThat(templates.get(1).getLabel(), is("second"));
        assertThat(templates.get(1).getNodeDiskPools().get(0).getDiskPoolRefId(), is("dp3"));
        nodeDisk = templates.get(1).getNodeDiskPools().get(0).getNodeDisks().get(0);
        assertThat(nodeDisk.getDiskRefId(), is("dp3refid1"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template31"));
        nodeDisk = templates.get(1).getNodeDiskPools().get(0).getNodeDisks().get(1);
        assertThat(nodeDisk.getDiskRefId(), is("dp3refid2"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template32"));
        assertThat(templates.get(1).getNodeDiskPools().get(1).getDiskPoolRefId(), is("dp4"));
        nodeDisk = templates.get(1).getNodeDiskPools().get(1).getNodeDisks().get(0);
        assertThat(nodeDisk.getDiskRefId(), is("dp4refid1"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template41"));
        // Third template test
        assertThat(templates.get(2).getLabel(), is("third"));
        assertThat(templates.get(2).getNodeDiskPools().get(0).getDiskPoolRefId(), is("dp5"));
        nodeDisk = templates.get(2).getNodeDiskPools().get(0).getNodeDisks().get(0);
        assertThat(nodeDisk.getDiskRefId(), is("dp5refid1"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template51"));
        nodeDisk = templates.get(2).getNodeDiskPools().get(0).getNodeDisks().get(1);
        assertThat(nodeDisk.getDiskRefId(), is("dp5refid2"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template52"));
        assertThat(templates.get(2).getNodeDiskPools().get(1).getDiskPoolRefId(), is("dp6"));
        nodeDisk = templates.get(2).getNodeDiskPools().get(1).getNodeDisks().get(0);
        assertThat(nodeDisk.getDiskRefId(), is("dp6refid1"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template61"));
        // Fourth template test
        assertThat(templates.get(3).getLabel(), is("fourth"));
        assertThat(templates.get(3).getNodeDiskPools().get(0).getDiskPoolRefId(), is("dp7"));
        nodeDisk = templates.get(3).getNodeDiskPools().get(0).getNodeDisks().get(0);
        assertThat(nodeDisk.getDiskRefId(), is("dp7refid1"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template71"));
        nodeDisk = templates.get(3).getNodeDiskPools().get(0).getNodeDisks().get(1);
        assertThat(nodeDisk.getDiskRefId(), is("dp7refid2"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template72"));
        assertThat(templates.get(3).getNodeDiskPools().get(1).getDiskPoolRefId(), is("dp8"));
        nodeDisk = templates.get(3).getNodeDiskPools().get(1).getNodeDisks().get(0);
        assertThat(nodeDisk.getDiskRefId(), is("dp8refid1"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template81"));
        // Fifth template test
        assertThat(templates.get(4).getLabel(), is("fifth"));
        assertThat(templates.get(4).getNodeDiskPools().get(0).getDiskPoolRefId(), is("dp9"));
        nodeDisk = templates.get(4).getNodeDiskPools().get(0).getNodeDisks().get(0);
        assertThat(nodeDisk.getDiskRefId(), is("dp9refid1"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template91"));
        nodeDisk = templates.get(4).getNodeDiskPools().get(0).getNodeDisks().get(1);
        assertThat(nodeDisk.getDiskRefId(), is("dp9refid2"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template92"));
        assertThat(templates.get(4).getNodeDiskPools().get(1).getDiskPoolRefId(), is("dp10"));
        nodeDisk = templates.get(4).getNodeDiskPools().get(1).getNodeDisks().get(0);
        assertThat(nodeDisk.getDiskRefId(), is("dp10refid1"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template101"));
        nodeDisk = templates.get(4).getNodeDiskPools().get(1).getNodeDisks().get(1);
        assertThat(nodeDisk.getDiskRefId(), is("dp10refid2"));
        assertThat(nodeDisk.getNodeMountPoint(), is("/tmp/template102"));
    }

    @Test
    public void shouldSupportConfigurationAsCodeMasterProperty() throws Exception {
        Computer computer = j.getInstance().getComputer("");
        Node node = computer.getNode();
        List<NodeDiskPool> nodeDiskPools = node.getNodeProperties().get(ExternalWorkspaceProperty.class).getNodeDiskPools();
        assertThat(nodeDiskPools.get(0).getDiskPoolRefId(), is("master-node-id"));
        assertThat(nodeDiskPools.get(0).getNodeDisks().get(0).getDiskRefId(), is("master-node-disk"));
        assertThat(nodeDiskPools.get(0).getNodeDisks().get(0).getNodeMountPoint(), is("/tmp/master-node"));
    }

    @Test
    public void shouldSupportConfigurationAsCodeAgentProperty() throws Exception {
        Computer computer = j.getInstance().getComputer("static-agent");
        Node node = computer.getNode();
        List<NodeDiskPool> nodeDiskPools = node.getNodeProperties().get(ExternalWorkspaceProperty.class).getNodeDiskPools();
        assertThat(nodeDiskPools.get(0).getDiskPoolRefId(), is("localhostdiskpool"));
        assertThat(nodeDiskPools.get(0).getNodeDisks().get(0).getDiskRefId(), is("localdisk"));
        assertThat(nodeDiskPools.get(0).getNodeDisks().get(0).getNodeMountPoint(), is("/tmp/localdisk"));
    }

    @Test
    public void exportGlobalConfigurationDiskPools() throws Exception {
        ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        ConfigurationContext context = new ConfigurationContext(registry);
        CNode yourAttribute = getUnclassifiedRoot(context).get("exwsGlobalConfigurationDiskPools");

        String exported = toYamlString(yourAttribute);

        String expected = toStringFromYamlFile(this, "global-casc-diskpools-expected.yaml");

        assertThat(exported, is(expected));
    }

    @Test
    public void exportGlobalConfigurationTemplates() throws Exception {
        ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        ConfigurationContext context = new ConfigurationContext(registry);
        CNode yourAttribute = getUnclassifiedRoot(context).get("exwsGlobalConfigurationTemplates");

        String exported = toYamlString(yourAttribute);

        String expected = toStringFromYamlFile(this, "global-casc-templates-expected.yaml");

        assertThat(exported, is(expected));
    }
}
