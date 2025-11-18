package com.group5.csv.core;

import com.group5.csv.schema.DateSpec;
import com.group5.csv.schema.DateTimeSpec;
import com.group5.csv.schema.DecimalSpec;
import com.group5.csv.validation.ValidationError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Field interface default behaviour.
 *
 * Since Field is an interface, we use a small test-only implementation
 * to exercise the default methods (especially valueAs()).
 */
class FieldTest {

    /**
     * Simple test implementation of Field for exercising default methods.
     */
    private static final class TestField implements Field {
        private final int index;
        private final String name;
        private final String raw;
        private final FieldType type;
        private final boolean missing;
        private final boolean valid;
        private final List<ValidationError> errors;
        private final Object value;
        private final DecimalSpec decimalSpec;
        private final DateSpec dateSpec;
        private final DateTimeSpec dateTimeSpec;

        TestField(Object value, FieldType type) {
            this(0, "field", "", type, false, true, List.of(),
                    value, null, null, null);
        }

        TestField(int index,
                  String name,
                  String raw,
                  FieldType type,
                  boolean missing,
                  boolean valid,
                  List<ValidationError> errors,
                  Object value,
                  DecimalSpec decimalSpec,
                  DateSpec dateSpec,
                  DateTimeSpec dateTimeSpec) {

            this.index = index;
            this.name = name;
            this.raw = raw;
            this.type = type;
            this.missing = missing;
            this.valid = valid;
            this.errors = errors;
            this.value = value;
            this.decimalSpec = decimalSpec;
            this.dateSpec = dateSpec;
            this.dateTimeSpec = dateTimeSpec;
        }

        @Override public int index() { return index; }
        @Override public String name() { return name; }
        @Override public String raw() { return raw; }
        @Override public FieldType type() { return type; }
        @Override public boolean isMissing() { return missing; }
        @Override public boolean isValid() { return valid; }
        @Override public List<ValidationError> errors() { return errors; }
        @Override public Object value() { return value; }
        @Override public DecimalSpec decimalSpec() { return decimalSpec; }
        @Override public DateSpec dateSpec() { return dateSpec; }
        @Override public DateTimeSpec dateTimeSpec() { return dateTimeSpec; }
    }

    @Test
    void valueAsReturnsNullWhenValueIsNull() {
        Field field = new TestField(null, FieldType.STRING);

        String result = field.valueAs(String.class);

        assertNull(result, "valueAs() should return null when underlying value is null");
    }

    @Test
    void valueAsCastsToRequestedTypeWhenCompatible() {
        Field field = new TestField(123, FieldType.INT);

        Integer intValue = field.valueAs(Integer.class);

        assertEquals(123, intValue);
    }

    @Test
    void valueAsThrowsWhenRequestedTypeIsIncompatible() {
        Field field = new TestField("hello", FieldType.STRING);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> field.valueAs(Integer.class),
                "valueAs() should throw when requested type is incompatible"
        );

        // Optional: assert that the error message is helpful
        assertTrue(ex.getMessage().contains("java.lang.String"),
                "Error message should mention actual value type");
        assertTrue(ex.getMessage().contains(Integer.class.getName()),
                "Error message should mention requested type");
    }

    @Test
    void basicMetadataAccessorsReturnConfiguredValues() {
        Field field = new TestField(
                5,
                "age",
                " 44 ",
                FieldType.INT,
                false,
                true,
                List.of(),
                44,
                null,
                null,
                null
        );

        assertEquals(5, field.index());
        assertEquals("age", field.name());
        assertEquals(" 44 ", field.raw());
        assertEquals(FieldType.INT, field.type());
        assertFalse(field.isMissing());
        assertTrue(field.isValid());
        assertTrue(field.errors().isEmpty());
        assertEquals(44, field.value());
    }
}
