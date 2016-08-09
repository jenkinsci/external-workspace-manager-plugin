package org.jenkinsci.plugins.ewm.utils;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link FormValidationUtil}.
 *
 * @author Alexandru Somai
 */
public class FormValidationUtilTest {

    private static final String UNSAFE_SYMBOL_MSG = "It may be unsafe to use standalone $ symbol for workspace template. It is recommended to use ${ } instead";
    private static final String NOT_VALID_PARENTHESES = "The workspace template parentheses are not valid";
    private static final String NOT_RELATIVE_PATH = "Must be a relative path";

    @Test
    public void validTemplate() {
        FormValidation formValidation = FormValidationUtil.validateWorkspaceTemplate("${JOB_NAME}");
        assertThat(formValidation, is(FormValidation.ok()));
    }

    @Test
    public void relativePath() {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        FormValidation formValidation = FormValidationUtil.validateWorkspaceTemplate("/${JOB_NAME}");
        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), NOT_RELATIVE_PATH), is(1));
    }

    @Test
    public void unsafeSymbol() {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        FormValidation formValidation = FormValidationUtil.validateWorkspaceTemplate("$JOB_NAME/$BUILD_NUMBER");
        assertThat(formValidation.kind, is(FormValidation.Kind.WARNING));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), UNSAFE_SYMBOL_MSG), is(1));
    }

    @Test
    public void multipleUnsafeSymbols() {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        FormValidation formValidation = FormValidationUtil.validateWorkspaceTemplate("$$$$$$");
        assertThat(formValidation.kind, is(FormValidation.Kind.WARNING));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), UNSAFE_SYMBOL_MSG), is(1));
    }

    @Test
    public void notValidParentheses() {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        FormValidation formValidation = FormValidationUtil.validateWorkspaceTemplate("${{foobar");
        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), NOT_VALID_PARENTHESES), is(1));
    }

    @Test
    public void toManyEnclosingParentheses() {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        FormValidation formValidation = FormValidationUtil.validateWorkspaceTemplate("}}}}foobar");
        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), NOT_VALID_PARENTHESES), is(1));
    }

    @Test
    public void wrongOrderOfParentheses() {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        FormValidation formValidation = FormValidationUtil.validateWorkspaceTemplate("}{");
        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), NOT_VALID_PARENTHESES), is(1));
    }

    @Test
    public void wrongOrderOfParenthesesWithUnsafeSymbol() {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        FormValidation formValidation = FormValidationUtil.validateWorkspaceTemplate("$}{");
        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), UNSAFE_SYMBOL_MSG), is(1));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), NOT_VALID_PARENTHESES), is(1));
    }

    @Test
    public void notValidParenthesesWithUnsafeSymbol() {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        FormValidation formValidation = FormValidationUtil.validateWorkspaceTemplate("${foobar}/$foo/${bar");
        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), UNSAFE_SYMBOL_MSG), is(1));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), NOT_VALID_PARENTHESES), is(1));
    }

    @Test
    public void notRelativePathWithNotValidParenthesesAndUnsafeSymbol() {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        FormValidation formValidation = FormValidationUtil.validateWorkspaceTemplate("/${foobar/$foo");
        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), NOT_RELATIVE_PATH), is(1));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), NOT_VALID_PARENTHESES), is(1));
        assertThat(StringUtils.countMatches(formValidation.renderHtml(), UNSAFE_SYMBOL_MSG), is(1));
    }
}
