package org.jenkinsci.plugins.ewm.strategies;

import hudson.AbortException;
import org.jenkinsci.plugins.ewm.TestUtil;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;

import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MostUsableSpaceStrategy}.
 *
 * @author Alexandru Somai
 */
public class MostUsableSpaceStrategyTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MostUsableSpaceStrategy strategy;

    @Before
    public void setUp() {
        strategy = spy(new MostUsableSpaceStrategy());
    }

    @Test
    public void allocateMostUsableSpace() throws Exception {
        Disk disk1 = TestUtil.createDisk();
        Disk disk2 = TestUtil.createDisk();
        Disk disk3 = TestUtil.createDisk();

        when(strategy.retrieveUsableSpace(disk1)).thenReturn(1L);
        when(strategy.retrieveUsableSpace(disk2)).thenReturn(200L);
        when(strategy.retrieveUsableSpace(disk3)).thenReturn(3L);

        Disk allocatedDisk = strategy.allocateDisk(Arrays.asList(disk1, disk2));
        assertThat(allocatedDisk, is(disk2));
    }

    @Test
    public void missingMasterMountPoint() throws Exception {
        Disk disk = new Disk("disk", null, null, null, null);

        thrown.expect(AbortException.class);
        thrown.expectMessage(format("Mounting point from Master to the disk is not defined for Disk ID '%s'", disk.getDiskId()));
        strategy.allocateDisk(Collections.singletonList(disk));
    }

    @Test
    public void estimatedWorkspaceSizeGreaterThanUsableSpace() throws Exception {
        long estimatedWorkspaceSize = 200L;
        strategy.setEstimatedWorkspaceSize(estimatedWorkspaceSize);
        Disk disk = TestUtil.createDisk();

        when(strategy.retrieveUsableSpace(disk)).thenReturn(100000L);

        thrown.expect(AbortException.class);
        thrown.expectMessage(format("The selected Disk with the most usable space doesn't have at least %s MB space", estimatedWorkspaceSize));
        strategy.allocateDisk(Collections.singletonList(disk));
    }
}
