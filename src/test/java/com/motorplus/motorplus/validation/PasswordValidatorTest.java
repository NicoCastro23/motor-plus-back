package com.motorplus.motorplus.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PasswordValidatorTest {

    private PasswordValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void isValid_returnsFalse_forNull() {
        assertThat(validator.isValid(null, context)).isFalse();
    }

    @Test
    void isValid_returnsFalse_forEmptyString() {
        assertThat(validator.isValid("", context)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ab1!", "A1!a", "Abc1!"})
    void isValid_returnsFalse_whenShorterThan8Characters(String shortPassword) {
        assertThat(validator.isValid(shortPassword, context)).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenNoUppercase() {
        assertThat(validator.isValid("abcdef1!", context)).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenNoLowercase() {
        assertThat(validator.isValid("ABCDEF1!", context)).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenNoDigit() {
        assertThat(validator.isValid("Abcdefg!", context)).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenNoSpecialCharacter() {
        assertThat(validator.isValid("Abcdef12", context)).isFalse();
    }

    @Test
    void isValid_returnsTrue_forMinimalValidPassword() {
        // Exactly 8 chars: uppercase + lowercase + digit + special
        assertThat(validator.isValid("Abcde1!x", context)).isTrue();
    }

    @Test
    void isValid_returnsTrue_forLongComplexPassword() {
        assertThat(validator.isValid("MyStr0ng!Pass#2024", context)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Secure1@",
        "P@ssw0rd",
        "Hello#1World",
        "Abc123$xyz"
    })
    void isValid_returnsTrue_forVariousValidPasswords(String password) {
        assertThat(validator.isValid(password, context)).isTrue();
    }

    @Test
    void isValid_returnsFalse_exactly7CharsWithAllRequirements() {
        // 7 characters but meets all character class requirements
        assertThat(validator.isValid("Abc1!xy", context)).isFalse();
    }

    @Test
    void isValid_returnsTrue_passwordWith8CharsAndAllSpecialVariants() {
        // Validates several special chars from the regex
        assertThat(validator.isValid("Valid1@!", context)).isTrue();
        assertThat(validator.isValid("Valid1#$", context)).isTrue();
        assertThat(validator.isValid("Valid1^&", context)).isTrue();
        assertThat(validator.isValid("Valid1()", context)).isTrue();
    }
}
