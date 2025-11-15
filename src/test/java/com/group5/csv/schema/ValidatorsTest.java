package com.group5.csv.schema;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.PatternSyntaxException;

import com.group5.csv.schema.Validators;
import com.group5.csv.schema.Validators.Validator;


class ValidatorsTest {

    // ---------------------------------------------------------------------
    // required()
    // ---------------------------------------------------------------------

    @Test
    void requiredRejectsNull() {
        Validator<Object> v = Validators.required();

        assertThrows(IllegalArgumentException.class, () -> v.validate(null));
    }

    @Test
    void requiredRejectsBlankString() {
        Validator<Object> v = Validators.required();

        assertThrows(IllegalArgumentException.class, () -> v.validate(""));
        assertThrows(IllegalArgumentException.class, () -> v.validate("   "));
    }

    @Test
    void requiredAcceptsNonNullNonBlankValues() {
        Validator<Object> v = Validators.required();

        assertDoesNotThrow(() -> v.validate("hello"));
        assertDoesNotThrow(() -> v.validate(123));
    }

    // ---------------------------------------------------------------------
    // min() / max() for BigDecimal
    // ---------------------------------------------------------------------

    @Test
    void minAllowsEqualAndAbove() {
        BigDecimal min = new BigDecimal("10.00");
        Validator<BigDecimal> v = Validators.min(min);

        assertDoesNotThrow(() -> v.validate(new BigDecimal("10.00")));
        assertDoesNotThrow(() -> v.validate(new BigDecimal("10.01")));
    }

    @Test
    void minRejectsBelow() {
        BigDecimal min = new BigDecimal("10.00");
        Validator<BigDecimal> v = Validators.min(min);

        assertThrows(IllegalArgumentException.class,
                () -> v.validate(new BigDecimal("9.99")));
    }

    @Test
    void maxAllowsEqualAndBelow() {
        BigDecimal max = new BigDecimal("100.00");
        Validator<BigDecimal> v = Validators.max(max);

        assertDoesNotThrow(() -> v.validate(new BigDecimal("100.00")));
        assertDoesNotThrow(() -> v.validate(new BigDecimal("99.99")));
    }

    @Test
    void maxRejectsAbove() {
        BigDecimal max = new BigDecimal("100.00");
        Validator<BigDecimal> v = Validators.max(max);

        assertThrows(IllegalArgumentException.class,
                () -> v.validate(new BigDecimal("100.01")));
    }

    @Test
    void minAndMaxIgnoreNullValues() {
        Validator<BigDecimal> v = Validators.and(
                Validators.min(new BigDecimal("0")),
                Validators.max(new BigDecimal("10"))
        );

        // non-required validators ignore null
        assertDoesNotThrow(() -> v.validate(null));
    }

    // ---------------------------------------------------------------------
    // length()
    // ---------------------------------------------------------------------

    @Test
    void lengthRejectsInvalidBoundaries() { // Validate defensive checks: length() must fail fast on impossible ranges.
        assertThrows(IllegalArgumentException.class,
                () -> Validators.length(-1, 5));
        assertThrows(IllegalArgumentException.class,
                () -> Validators.length(10, 5));
    }

    @Test
    void lengthRejectsOutOfRange() {
        Validator<String> v = Validators.length(2, 4);

        assertThrows(IllegalArgumentException.class, () -> v.validate(""));      // len 0
        assertThrows(IllegalArgumentException.class, () -> v.validate("A"));     // len 1
        assertThrows(IllegalArgumentException.class, () -> v.validate("ABCDE")); // len 5
    }

    @Test
    void lengthAcceptsWithinRange() {
        Validator<String> v = Validators.length(2, 4);

        assertDoesNotThrow(() -> v.validate("AB"));    // len 2
        assertDoesNotThrow(() -> v.validate("ABC"));   // len 3
        assertDoesNotThrow(() -> v.validate("ABCD"));  // len 4
    }

    @Test
    void lengthIgnoresNull() {
        Validator<String> v = Validators.length(1, 5);

        assertDoesNotThrow(() -> v.validate(null));
    }

    // ---------------------------------------------------------------------
    // regex()
    // ---------------------------------------------------------------------

    @Test
    void regexAcceptsMatchingStrings() {
        Validator<String> v = Validators.regex("[A-Z]{2}[0-9]{2}");

        assertDoesNotThrow(() -> v.validate("AB12"));
        assertDoesNotThrow(() -> v.validate("ZZ99"));
    }

    @Test
    void regexRejectsNonMatchingStrings() {
        Validator<String> v = Validators.regex("[A-Z]{2}[0-9]{2}");

        assertThrows(IllegalArgumentException.class, () -> v.validate("ab12")); // lower-case
        assertThrows(IllegalArgumentException.class, () -> v.validate("ABC"));  // too short
        assertThrows(IllegalArgumentException.class, () -> v.validate("1234")); // no letters
    }

    @Test
    void regexIgnoresNull() {
        Validator<String> v = Validators.regex("[A-Z]+");

        assertDoesNotThrow(() -> v.validate(null));
    }

    @Test
    void regexThrowsOnInvalidPattern() {
        assertThrows(PatternSyntaxException.class, () -> Validators.regex("[unclosed"));
    }

    // ---------------------------------------------------------------------
    // enumOf()
    // ---------------------------------------------------------------------

    @Test
    void enumOfAcceptsAllowedValues() {
        Validator<String> v = Validators.enumOf("YES", "NO");

        assertDoesNotThrow(() -> v.validate("YES"));
        assertDoesNotThrow(() -> v.validate("NO"));
    }

    @Test
    void enumOfRejectsOtherValues() {
        Validator<String> v = Validators.enumOf("YES", "NO");

        assertThrows(IllegalArgumentException.class, () -> v.validate("MAYBE"));
    }

    @Test
    void enumOfIgnoresNull() {
        Validator<String> v = Validators.enumOf("A", "B");

        assertDoesNotThrow(() -> v.validate(null));
    }

    // ---------------------------------------------------------------------
    // custom()
    // ---------------------------------------------------------------------

    @Test
    void customValidatorUsesPredicateAndMessage() {
        Validator<String> v = Validators.custom(
                s -> s != null && s.startsWith("A"),
                "Must start with A"
        );

        assertDoesNotThrow(() -> v.validate("Alpha"));
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> v.validate("Beta")
        );
        assertTrue(ex.getMessage().contains("Must start with A"));
    }

    @Test
    void customIgnoresNull() {
        Validator<String> v = Validators.custom(
                s -> s.length() > 3,
                "Too short"
        );

        // predicate is never called, null is ignored
        assertDoesNotThrow(() -> v.validate(null));
    }

    // ---------------------------------------------------------------------
    // and() / or() / not()
    // ---------------------------------------------------------------------

    @Test
    void andCombinesValidatorsAllMustPass() {
        Validator<String> v = Validators.and(
                (String s) -> { if (s == null) throw new IllegalArgumentException("null"); },
                Validators.length(2, 5)
        );

        assertDoesNotThrow(() -> v.validate("ABCD"));
        assertThrows(IllegalArgumentException.class, () -> v.validate(null));
        assertThrows(IllegalArgumentException.class, () -> v.validate("A"));     // length too short
        assertThrows(IllegalArgumentException.class, () -> v.validate("ABCDEFG"));// too long
    }

    @Test
    void orPassesIfAnyValidatorPasses() {
        Validator<String> v = Validators.or(
                Validators.regex("[0-9]+"),
                Validators.enumOf("foo", "bar")
        );

        assertDoesNotThrow(() -> v.validate("12345")); // matches first
        assertDoesNotThrow(() -> v.validate("foo"));   // matches second

        // neither regex nor enumOf matches
        assertThrows(IllegalArgumentException.class, () -> v.validate("xyz"));
    }

    @Test
    void notInvertsValidatorLogic() {
        Validator<String> inner = Validators.regex("OK");
        Validator<String> notOk = Validators.not(inner, "Value must NOT be 'OK'");

        // inner passes -> NOT fails
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> notOk.validate("OK")
        );
        assertTrue(ex1.getMessage().contains("must NOT"));

        // inner fails -> NOT passes
        assertDoesNotThrow(() -> notOk.validate("BAD"));
    }

    // ---------------------------------------------------------------------
    // validateAll()
    // ---------------------------------------------------------------------

    @Test
    void validateAllCollectsAllErrorMessages() {
        // both validators are Validator<String>
        List<Validator<String>> validators = List.of(
                Validators.length(2, 4),
                Validators.regex("[A-Z]+")
        );

        List<String> errors = new ArrayList<>();

        // "a1" has length 2 (OK) but fails regex [A-Z]+
        Validators.validateAll("a1", validators, errors);

        assertFalse(errors.isEmpty());
        // expect at least one error (from regex)
        assertEquals(1, errors.size());
    }



}
