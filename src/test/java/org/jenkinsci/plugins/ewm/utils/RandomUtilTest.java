package org.jenkinsci.plugins.ewm.utils;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link RandomUtil}.
 *
 * @author Alexandru Somai
 */
public class RandomUtilTest {

    @Test
    public void generateRandomHexStringLength() {
        String generatedString = RandomUtil.generateRandomHexString(32);
        assertThat(generatedString.length(), is(32));
    }
}
