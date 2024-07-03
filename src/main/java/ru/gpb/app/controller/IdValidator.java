package ru.gpb.app.controller;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
public class IdValidator implements ConstraintValidator<ValidId, String> {

    @Override
    public void initialize(ValidId constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String id, ConstraintValidatorContext context) {
        if (id == null || id.trim().isEmpty() || id.length() < 5) {
            log.error("String is null, empty, or too short while validating");
            return false;
        }

        for (char ch : id.toCharArray()) {
            if (!(Character.isLetterOrDigit(ch) || ch == '_')) {
                log.error("Invalid character found: {}", ch);
                return false;
            }
        }

        log.info("String is valid");
        return true;
    }
}
