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
 * Tests for {@link FastestReadSpeedStrategy}.
 *
 * @author Alexandru Somai
 */
public class FastestReadSpeedStrategyTest extends AbstractDiskSpeedStrategyTest<FastestReadSpeedStrategy> {

    @Before
    public void setUp() {
        strategy = spy(new FastestReadSpeedStrategy());
    }

    @Test
    public void allocateHighestReadSpeed() throws Exception {
        Disk disk1 = TestUtil.createDisk(new UserProvidedDiskInfo(2, 10));
        Disk disk2 = TestUtil.createDisk(new UserProvidedDiskInfo(7, 20));
        Disk disk3 = TestUtil.createDisk(new UserProvidedDiskInfo(3, 30));

        Disk allocateDisk = strategy.allocateDisk(Arrays.asList(disk1, disk2, disk3));
        assertThat(allocateDisk, is(disk2));
    }
}
