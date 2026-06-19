package com.codeygen.clubos.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CollegeMailValidator.class)
public @interface CollegeMail {
    String message()

    default "Invalid college mail";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
