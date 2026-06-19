package com.codeygen.clubos.utils;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CollegeMailValidator implements ConstraintValidator<CollegeMail, String> {
    private static final Set<String> VALID_DOMAINS = Set.of("vitstudent.ac.in", "vit.ac.in");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true;
        }

        return VALID_DOMAINS.stream()
                .anyMatch(domain -> email.endsWith("@" + domain));
    }
}
