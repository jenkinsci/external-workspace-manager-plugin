package org.jenkinsci.plugins.ewm.strategies;

import org.jenkinsci.plugins.ewm.TestUtil;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import org.jenkinsci.plugins.ewm.providers.UserProvidedDiskInfo;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;

/**
 * Tests for {@link FastestWriteSpeedStrategy}.
 *
 * @author Alexandru Somai
 */
public class FastestWriteSpeedStrategyTest extends AbstractDiskSpeedStrategyTest<FastestWriteSpeedStrategy> {

    @Before
    public void setUp() {
        strategy = spy(new FastestWriteSpeedStrategy());
    }

    @Test
    public void allocateHighestWriteSpeed() throws Exception {
        Disk disk1 = TestUtil.createDisk(new UserProvidedDiskInfo(10, 2));
        Disk disk2 = TestUtil.createDisk(new UserProvidedDiskInfo(20, 7));
        Disk disk3 = TestUtil.createDisk(new UserProvidedDiskInfo(30, 3));

        Disk allocateDisk = strategy.allocateDisk(Arrays.asList(disk1, disk2, disk3));
        assertThat(allocateDisk, is(disk2));
    }
}
