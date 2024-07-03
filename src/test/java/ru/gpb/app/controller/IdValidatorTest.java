package ru.gpb.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

public class IdValidatorTest {

    private IdValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    public void setUp() {
        validator = new IdValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    public void validId1() {
        boolean result = validator.isValid("valid_id_123", context);

        assertThat(result).isTrue();
    }

    @Test
    public void validId2() {
        boolean result = validator.isValid("user1_", context);

        assertThat(result).isTrue();
    }

    @Test
    public void nullId() {
        boolean result = validator.isValid(null, context);

        assertThat(result).isFalse();
    }

    @Test
    public void emptyId() {
        boolean result = validator.isValid("", context);

        assertThat(result).isFalse();
    }

    @Test
    public void wayTooShortId() {
        boolean result = validator.isValid("abcd", context);

        assertThat(result).isFalse();
    }

    @Test
    public void idWithSpecialSymbols1() {
        boolean result = validator.isValid("invalid$id", context);

        assertThat(result).isFalse();
        assertFalse(validator.isValid("user@name", context));
    }

    @Test
    public void idWithSpecialSymbols2() {
        boolean result = validator.isValid("user@name", context);

        assertThat(result).isFalse();
    }

    @Test
    public void idWithSpaces() {
        boolean result = validator.isValid("user name", context);

        assertThat(result).isFalse();
    }
}