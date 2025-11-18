package com.group5.csv.schema;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Validators
 * ----------------------------------------------------------------------
 * A library of reusable validation helpers for schema-based CSV mapping.
 *
 * The basic idea:
 *
 *    A Validator<T> is a small function that can "check" a value of type T
 *    and throw an IllegalArgumentException if the value is invalid.
 *
 *    Core validators: required, min, max, regex, enumOf, length, custom.
 *    Combinators: and, or, not.
 *
 *    Non-required validators MUST treat null as "no value" and do nothing.
 *
 * Typical usage:
 *
 *   Validator<String> v = Validators.and(
 *       Validators.required(),
 *       Validators.length(1, 20),
 *       Validators.regex("[A-Z0-9]+")
 *   );
 *
 *   v.validate(rawValue); // throws IllegalArgumentException on failure
 *
 * RowBuilder or Schema code can later catch these exceptions and convert
 * them into ValidationError objects, or run in a fail-fast mode by letting
 * them propagate.
 *
 */
public final class Validators {

    private Validators() {
        // utility class; no instances
    }

    /**
     * Functional interface for a single validation rule.
     *
     * Implementations must:
     *  - perform a check on the given value
     *  - throw IllegalArgumentException with a meaningful message if invalid
     *  - do nothing if the value is valid
     */
    @FunctionalInterface
    public interface Validator<T> {
        void validate(T value) throws IllegalArgumentException;
    }

    // ---------------------------------------------------------------------
    // Basic building blocks
    // ---------------------------------------------------------------------

    /**
     * Required validator:
     *  For any reference type: value must not be null.
     *  For String specifically: value must not be null or blank.
     */
    public static Validator<Object> required() {
        return value -> {
            if (value == null) {
                throw new IllegalArgumentException("Value is required");
            }
            if (value instanceof String s && s.isBlank()) {
                throw new IllegalArgumentException("Blank string is not allowed");
            }
        };
    }

    /**
     * Minimum value validator for BigDecimal.
     * Null values are ignored (can be forced by using required()).
     */
    public static Validator<BigDecimal> min(BigDecimal min) {
        Objects.requireNonNull(min, "min must not be null");
        return value -> {
            if (value == null) return;
            if (value.compareTo(min) < 0) {
                throw new IllegalArgumentException("Value " + value + " is below minimum " + min);
            }
        };
    }

    /**
     * Maximum value validator for BigDecimal.
     * Null values are ignored (can be forced by using required()).
     */
    public static Validator<BigDecimal> max(BigDecimal max) {
        Objects.requireNonNull(max, "max must not be null");
        return value -> {
            if (value == null) return;
            if (value.compareTo(max) > 0) {
                throw new IllegalArgumentException("Value " + value + " is above maximum " + max);
            }
        };
    }

    /**
     * String length validator: inclusive [min, max].
     * Null values are ignored (can be forced by using required()).
     */
    public static Validator<String> length(int min, int max) {
        if (min < 0 || max < min) {
            throw new IllegalArgumentException("Invalid length boundaries. Limits are: min=" + min + ", max=" + max);
        }
        return value -> {
            if (value == null) return;
            int len = value.length();
            if (len < min || len > max) {
                throw new IllegalArgumentException(
                        "Length " + len + " is out of range [" + min + "," + max + "]"
                );
            }
        };
    }

    /**
     * Regex validator for Strings. Uses java.util.regex.Pattern.
     * Null values are ignored (can be forced by using required()).
     */
    public static Validator<String> regex(String regex) {
        Objects.requireNonNull(regex, "regex must not be null");
        Pattern pattern = Pattern.compile(regex);
        return value -> {
            if (value == null) return;
            if (!pattern.matcher(value).matches()) {
                throw new IllegalArgumentException("Value '" + value + "' does not match pattern: " + regex);
            }
        };
    }

    /**
     * Enum-of validator for Strings.
     * Checks that the value is one of the allowedTokens (case-sensitive).
     * Null values are ignored (can be forced by using required()).
     */
    public static Validator<String> enumOf(String... allowedTokens) {
        if (allowedTokens == null || allowedTokens.length == 0) {
            throw new IllegalArgumentException("allowedTokens must not be empty");
        }
        Set<String> allowed = new HashSet<>(List.of(allowedTokens));
        return value -> {
            if (value == null) return;
            if (!allowed.contains(value)) {
                throw new IllegalArgumentException(
                        "Value '" + value + "' not in allowed set " + allowed
                );
            }
        };
    }

    /**
     * Custom validator based on a Predicate.
     * For help with Predicate, see: https://docs.oracle.com/javase/8/docs/api/javax/sql/rowset/Predicate.html
     *
     * If predicate returns false (and value is non-null), an error is thrown with the given message.
     * Null values are ignored (use required() to enforce presence).
     */
    public static <T> Validator<T> custom(Predicate<? super T> predicate, String message) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        Objects.requireNonNull(message, "message must not be null");
        return value -> {
            if (value == null) return;
            if (!predicate.test(value)) {
                throw new IllegalArgumentException(message);
            }
        };
    }

    // ---------------------------------------------------------------------
    // Composition helpers
    // ---------------------------------------------------------------------

    /**
     * Logical AND - multiple validators.
     * All validators are executed; the first one to throw stops the chain.
     */
    @SafeVarargs
    public static <T> Validator<T> and(Validator<? super T>... validators) {
        Objects.requireNonNull(validators, "validators must not be null");
        if (validators.length == 0) {
            throw new IllegalArgumentException("and() requires at least one validator");
        }
        return value -> {
            for (Validator<? super T> v : validators) {
                if (v == null) continue;
                // Cast is safe because we only ever pass T
                @SuppressWarnings("unchecked")
                Validator<T> vt = (Validator<T>) v;
                vt.validate(value);
            }
        };
    }

    /**
     * Logical OR - multiple validators.
     * Passes if at least one validator accepts the value.
     * Fails only if ALL validators fail.
     *
     * Implementation detail:
     *   - Runs each validator in order.
     *   - If any passes, we return.
     *   - If all throw, we rethrow the *last* exception.
     */
    @SafeVarargs
    public static <T> Validator<T> or(Validator<? super T>... validators) {
        Objects.requireNonNull(validators, "validators must not be null");
        if (validators.length == 0) {
            throw new IllegalArgumentException("or() requires at least one validator");
        }
        return value -> {
            IllegalArgumentException last = null;
            for (Validator<? super T> v : validators) {
                if (v == null) continue;
                @SuppressWarnings("unchecked")
                Validator<T> vt = (Validator<T>) v;
                try {
                    vt.validate(value);
                    // success → short-circuit
                    return;
                } catch (IllegalArgumentException ex) {
                    last = ex;
                }
            }
            if (last != null) {
                throw last;
            }
        };
    }

    /**
     * Logical NOT of a validator.
     * If the inner validator passes, this one fails; and vice versa.
     *
     * Note: null values are simply forwarded to the inner validator; if it ignores null, NOT will too.
     */
    public static <T> Validator<T> not(Validator<? super T> validator, String messageIfFails) {
        Objects.requireNonNull(validator, "validator must not be null");
        Objects.requireNonNull(messageIfFails, "messageIfFails must not be null");

        return value -> {
            boolean innerPassed = true;
            try {
                @SuppressWarnings("unchecked")
                Validator<T> vt = (Validator<T>) validator;
                vt.validate(value);
            } catch (IllegalArgumentException ex) {
                // inner failed -> NOT passes; don't throw
                innerPassed = false;
            }

            // If inner passed without throwing, NOT should fail
            if (innerPassed) {
                throw new IllegalArgumentException(messageIfFails);
            }
        };
    }



    // ---------------------------------------------------------------------
    // Small convenience wrappers for lists
    // ---------------------------------------------------------------------

    /**
     * Validate a value against a list of validators, collecting all failures
     * into the given list of error messages.
     *
     * This is a helper for lenient mode; callers can decide whether to
     * stop after the first error or collect up to a limiting maximum cap.
     */
    public static <T> void validateAll(T value,
                                       List<Validator<T>> validators,
                                       List<String> errorMessages) {
        Objects.requireNonNull(validators, "validators must not be null");
        Objects.requireNonNull(errorMessages, "errorMessages must not be null");

        for (Validator<T> v : validators) {
            if (v == null) continue;
            try {
                v.validate(value);
            } catch (IllegalArgumentException ex) {
                errorMessages.add(ex.getMessage());
            }
        }
    }

}