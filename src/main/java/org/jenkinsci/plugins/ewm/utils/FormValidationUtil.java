package org.jenkinsci.plugins.ewm.utils;

import hudson.Util;
import hudson.util.FormValidation;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link FormValidation} utility class.
 *
 * @author Alexandru Somai
 */
public final class FormValidationUtil {

    private FormValidationUtil() {
        // do not instantiate
    }

    /**
     * Validates the input String to match a specific workspace template.
     * Shows error messages if the input path is not relative, or if it doesn't enclose brackets correctly.
     * Shows warning message if the input template uses standalone '$' symbol instead of '${}'.
     * <p>
     * Note: It may show multiple error/warning messages for the same input String.
     * e.g. '/$abc{abc' has 2 error messages and 1 warning:
     * - error: not relative path
     * - error: not valid enclosing parentheses
     * - warning: unsafe standalone '$' usage
     *
     * @param value the input String to be checked
     * @return the FormValidation based on the input String
     */
    @Nonnull
    public static FormValidation validateWorkspaceTemplate(@Nonnull String value) {
        Set<FormValidation> formValidations = new HashSet<>();
        Set<String> existingMessages = new HashSet<>();

        if (!Util.isRelativePath(value)) {
            formValidations.add(FormValidation.error("Must be a relative path"));
        }

        String invalidParenthesesMsg = "The workspace template parentheses are not valid";
        int bracketCount = 0;
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {

            char c = chars[i];
            if (c == '{') {
                bracketCount++;
            } else if (c == '}') {
                bracketCount--;
            }

            if (bracketCount < 0) {
                if (existingMessages.add(invalidParenthesesMsg)) {
                    // hackish solution used to avoid adding same error message twice
                    // {@link FormValidation} doesn't implement hashCode, therefore I can't rely on using {@code Set} for unique elements
                    formValidations.add(FormValidation.error(invalidParenthesesMsg));
                }
            }

            if (c == '$' && i < chars.length - 1 && chars[i + 1] != '{') {
                String message = "It may be unsafe to use standalone '$' symbol for workspace template. " +
                        "It's recommended to use '${ }' instead";
                if (existingMessages.add(message)) {
                    formValidations.add(FormValidation.warning(message));
                }
            }
        }

        if (bracketCount != 0) {
            if (existingMessages.add(invalidParenthesesMsg)) {
                formValidations.add(FormValidation.error(invalidParenthesesMsg));
            }
        }

        return FormValidation.aggregate(formValidations);
    }
}
