package com.group5.csv.core;

import com.group5.csv.schema.DecimalSpec;
import com.group5.csv.schema.DateSpec;
import com.group5.csv.schema.DateTimeSpec;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enumeration of supported CSV field types with parsing and formatting logic.
 * <p>
 * Each type provides:
 * <ul>
 *   <li>{@code parse(String, Field)} - Converts CSV string to typed object</li>
 *   <li>{@code format(Object, Field)} - Converts typed object to CSV string</li>
 * </ul>
 * </p>
 * 
 * <p><b>Supported Types:</b></p>
 * <ul>
 *   <li>{@link #STRING} - Plain text (no conversion)</li>
 *   <li>{@link #INT} - 32-bit integer</li>
 *   <li>{@link #LONG} - 64-bit integer</li>
 *   <li>{@link #DOUBLE} - Double-precision floating point</li>
 *   <li>{@link #DECIMAL} - Arbitrary precision decimal (uses {@link DecimalSpec})</li>
 *   <li>{@link #BOOLEAN} - True/false values</li>
 *   <li>{@link #DATE} - Date-only (uses {@link DateSpec})</li>
 *   <li>{@link #DATETIME} - Date and time (uses {@link DateTimeSpec})</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Field field = new MockField(FieldType.INT);
 * Integer value = (Integer) FieldType.INT.parse("123", field);
 * String csv = FieldType.INT.format(value, field);
 * }</pre>
 * 
 * @author Michael McKibbin (initial implementation)
 * @author Edson Ferreira (completed missing types)
 * @see Field
 * @see DecimalSpec
 * @see DateTimeSpec
 * @see DateSpec
 */
public enum FieldType {

    STRING {
        @Override public Object parse(String raw, Field field) {
            return raw;
        }
        @Override public String format(Object value, Field field) {
            return value == null ? "" : value.toString();
        }
    },

    INT {
        @Override public Object parse(String raw, Field field) {
            if (raw == null) return null;
            String s = raw.trim();
            if (s.isEmpty()) return null;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer: " + raw, e);
            }
        }
        @Override public String format(Object value, Field field) {
            return value == null ? "" : value.toString();
        }
    },

    LONG {
        @Override public Object parse(String raw, Field field) {
            if (raw == null) return null;
            String s = raw.trim();
            if (s.isEmpty()) return null;
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid long: " + raw, e);
            }
        }
        @Override public String format(Object value, Field field) {
            return value == null ? "" : value.toString();
        }
    },

    DOUBLE {
        @Override public Object parse(String raw, Field field) {
            if (raw == null) return null;
            String s = raw.trim();
            if (s.isEmpty()) return null;
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid double: " + raw, e);
            }
        }
        @Override public String format(Object value, Field field) {
            return value == null ? "" : value.toString();
        }
    },

    DECIMAL {
        @Override public Object parse(String raw, Field field) {
            DecimalSpec spec = field.decimalSpec();
            return spec.parse(raw);
        }
        @Override public String format(Object value, Field field) {
            return field.decimalSpec().format((BigDecimal) value);
        }
    },

    BOOLEAN {
        @Override public Object parse(String raw, Field field) {
            if (raw == null) return null;
            String s = raw.trim().toLowerCase();
            return s.equals("true") || s.equals("1") || s.equals("y") || s.equals("yes");
        }
        @Override public String format(Object value, Field field) {
            if (value == null) return "";
            return ((Boolean) value) ? "true" : "false";
        }
    },

    DATE {
        @Override public Object parse(String raw, Field field) {
            return field.dateSpec().parse(raw);
        }
        @Override public String format(Object value, Field field) {
            if (value == null) return "";
            return field.dateSpec().format((LocalDate) value);
        }
    },

    DATETIME {
        @Override public Object parse(String raw, Field field) {
            return field.dateTimeSpec().parse(raw);
        }
        @Override public String format(Object value, Field field) {
            if (value == null) return "";
            return field.dateTimeSpec().format((LocalDateTime) value);
        }
    };

    /**
     * Parses a CSV field value into a typed object.
     * <p>
     * Null and empty strings are handled consistently across all types,
     * returning null. Whitespace is trimmed before parsing.
     * </p>
     * 
     * @param raw the raw CSV field value (may be null)
     * @param field the field configuration (provides specs for complex types)
     * @return the parsed typed object, or null if input is null/empty
     * @throws IllegalArgumentException if the value cannot be parsed
     */
    public abstract Object parse(String raw, Field field);

    /**
     * Formats a typed object into a CSV field value.
     * <p>
     * Null values are formatted as empty strings.
     * </p>
     * 
     * @param value the typed object to format (may be null)
     * @param field the field configuration (provides specs for complex types)
     * @return the formatted CSV string, or empty string if value is null
     */
    public abstract String format(Object value, Field field);
}
