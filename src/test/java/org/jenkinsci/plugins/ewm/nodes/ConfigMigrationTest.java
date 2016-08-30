package org.jenkinsci.plugins.ewm.nodes;

import hudson.model.Computer;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;

import java.util.List;

import org.jenkinsci.plugins.ewm.nodes.NodeDisk;

import org.junit.Rule;
import org.junit.Test;

import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThat;

/**
 * Tests the node configuration migration
 *
 * @author Martin d'Anjou
 */
public class ConfigMigrationTest {

    @Rule
    public JenkinsRule jr = new JenkinsRule();

    @LocalData
    @Test
    public void localRootPathToNodeMountPoint() throws Exception {

        // The node configuration version 1.0.0 contains a localRootPath field.
        // This tests that future versions load this field as nodeMountPoint,
        // in other words it tests NodeDisk.readResolve()

        Computer computer = jr.getInstance().getComputer("node1");
        Node node = computer.getNode();
        DescribableList<NodeProperty<?>,NodePropertyDescriptor> props = node.getNodeProperties();
        ExternalWorkspaceProperty exwsProp = props.get(ExternalWorkspaceProperty.class);

        List<NodeDiskPool> diskPools = exwsProp.getNodeDiskPools();
        assertThat(diskPools.size(), is(1));

        NodeDiskPool diskPool = diskPools.get(0);
        assertThat(diskPool.getDiskPoolRefId(), is ("dp1"));

        List<NodeDisk> disks = diskPool.getNodeDisks();
        assertThat(disks.size(), is(1));

        NodeDisk disk = disks.get(0);
        assertThat(disk.getDiskRefId(), is("d1"));
        assertThat(disk.getNodeMountPoint(), is("/tmp/dp1/d1"));
    }
}
