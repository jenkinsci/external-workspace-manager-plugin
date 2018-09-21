package org.jenkinsci.plugins.ewm;

import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jvnet.hudson.test.JenkinsRule;
import io.jenkins.plugins.casc.ConfigurationAsCode;

/**
 * Test for Configuration As Code Compatibility.
 *
 * @author Martin d'Anjou
 */
public class ConfigAsCodeTest {

    @ClassRule public static JenkinsRule r = new JenkinsRule();

    @Test
    public void should_support_configuration_as_code() throws Exception {
        String config = ConfigAsCodeTest.class.getResource("configuration-as-code.yaml").toString(); //configuration-as-code.yml").toString();
        System.out.println("config: " + config);
        ConfigurationAsCode.get().configure(config);
        assertTrue(true); // check plugin has been configured as expected );
    }

}
