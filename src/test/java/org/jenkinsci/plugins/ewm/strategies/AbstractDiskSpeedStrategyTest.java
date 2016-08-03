package org.jenkinsci.plugins.ewm.strategies;

import hudson.AbortException;
import org.jenkinsci.plugins.ewm.TestUtil;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

import static java.lang.String.format;
import static org.mockito.Mockito.when;

/**
 * Base test class for {@link AbstractDiskSpeedStrategy}.
 *
 * @author Alexandru Somai
 */
public abstract class AbstractDiskSpeedStrategyTest<T extends AbstractDiskSpeedStrategy> {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected T strategy;

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

        when(strategy.retrieveUsableSpace(disk)).thenReturn(100L);

        thrown.expect(AbortException.class);
        thrown.expectMessage(format("Couldn't find any Disk with at least %s KB usable space", estimatedWorkspaceSize));
        strategy.allocateDisk(Collections.singletonList(disk));
    }
}
