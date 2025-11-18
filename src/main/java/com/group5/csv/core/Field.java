package com.group5.csv.core;

import com.group5.csv.schema.DateSpec;
import com.group5.csv.schema.DateTimeSpec;
import com.group5.csv.schema.DecimalSpec;
import com.group5.csv.validation.ValidationError;

import java.util.List;

/**
 * Represents a single CSV cell.
 *
 * A Field provides:
 *  - the original raw text
 *  - its logical/column index and (optionally) name
 *  - its declared {@link FieldType}
 *  - access to type-specific specs (decimal/date/datetime)
 *  - the parsed value and validation information
 *
 * Implementations should be immutable and (ideally) thread-safe.
 */
public interface Field {

    /**
     * @return zero-based column index within the row, or -1 if unknown.
     */
    int index();

    /**
     * @return the column/field name (typically the header), or null/empty
     *         if this information is not available.
     */
    String name();

    /**
     * @return the raw CSV text for this cell (never null; use "" for empty).
     */
    String raw();

    /**
     * @return the FieldType that knows how to parse/format this field.
     */
    FieldType type();

    /**
     * @return true if this field should be treated as "missing"
     *         (e.g. raw is empty or only whitespace, depending on format).
     */
    boolean isMissing();

    /**
     * @return true if parsing/validation succeeded for this field.
     */
    boolean isValid();

    /**
     * @return an immutable list of validation errors. Empty list if valid.
     */
    List<ValidationError> errors();

    /**
     * @return the parsed value for this field, or null if missing/invalid.
     *         Runtime type depends on {@link #type()}:
     *
     *         STRING   -> String
     *         INT      -> Integer
     *         LONG     -> Long
     *         DOUBLE   -> Double
     *         DECIMAL  -> BigDecimal
     *         BOOLEAN  -> Boolean
     *         DATE     -> LocalDate
     *         DATETIME -> LocalDateTime
     */
    Object value();

    /**
     * Convenience helper to safely cast the value.
     */
    default <T> T valueAs(Class<T> clazz) {
        Object v = value();
        if (v == null) {
            return null;
        }
        if (!clazz.isInstance(v)) {
            throw new IllegalStateException(
                    "Field value is " + v.getClass().getName()
                            + " but " + clazz.getName() + " was requested");
        }
        return clazz.cast(v);
    }

    // ---- Type-specific specs used by FieldType enum ----

    /**
     * @return the DecimalSpec for DECIMAL fields.
     *         Implementations may return a default spec for other types.
     */
    DecimalSpec decimalSpec();

    /**
     * @return the DateSpec for DATE fields.
     *         Implementations may return a default spec for other types.
     */
    DateSpec dateSpec();

    /**
     * @return the DateTimeSpec for DATETIME fields.
     *         Implementations may return a default spec for other types.
     */
    DateTimeSpec dateTimeSpec();
}
