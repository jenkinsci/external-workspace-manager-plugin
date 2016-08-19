package org.jenkinsci.plugins.ewm.utils;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Random;

/**
 * Utility class for random generators.
 *
 * @author Alexandru Somai
 */
@Restricted(NoExternalUse.class)
public final class RandomUtil {

    private static final Random RANDOM = new Random();

    private RandomUtil() {
        // do not instantiate
    }

    /**
     * Generates a random hex String with the given length.
     *
     * @param numChars the length of the String to be generated
     * @return the random generated hex String
     */
    public static String generateRandomHexString(int numChars) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < numChars) {
            sb.append(Integer.toHexString(RANDOM.nextInt()));
        }

        return sb.toString().substring(0, numChars);
    }
}
