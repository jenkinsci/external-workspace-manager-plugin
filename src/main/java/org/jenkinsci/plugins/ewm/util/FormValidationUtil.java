package org.jenkinsci.plugins.ewm.util;

import hudson.util.FormValidation;

import javax.annotation.Nonnull;

/**
 * Utility class used for form validation.
 *
 * @author Alexandru Somai
 */
public final class FormValidationUtil {

    private FormValidationUtil() {
        // do not instantiate
    }

    /**
     * Checks if the given value is not empty.
     *
     * @param value the String value to be checked
     * @return a form validation error if the param is empty, ok otherwise
     */
    public static FormValidation doCheckValue(@Nonnull String value) {
        if (value.trim().isEmpty()) {
            return FormValidation.error("Required");
        }
        return FormValidation.ok();
    }
}
