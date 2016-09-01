package org.jenkinsci.plugins.ewm.utils;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Random;

/**
 * Various utility methods.
 *
 * @author Alexandru Somai
 */
@Restricted(NoExternalUse.class)
public final class Util {

    private static final Random RANDOM = new Random();

    private Util() {
        // do not instantiate
    }

    /**
     * Generated a random hex String with the given length.
     *
     * @param numChars the length of the String to be generated
     * @return the random generated HEX String
     */
    public static String generateRandomHexString(int numChars) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < numChars) {
            sb.append(Integer.toHexString(RANDOM.nextInt()));
        }

        return sb.toString().substring(0, numChars);
    }
}
