package com.motorplus.motorplus.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String UPPERCASE = ".*[A-Z].*";
    private static final String LOWERCASE = ".*[a-z].*";
    private static final String DIGIT     = ".*[0-9].*";
    private static final String SPECIAL   = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.length() < 8) return false;
        return password.matches(UPPERCASE)
            && password.matches(LOWERCASE)
            && password.matches(DIGIT)
            && password.matches(SPECIAL);
    }
}
