package com.group5.csv.core;

import com.group5.csv.schema.DecimalSpec;
import com.group5.csv.schema.DateSpec;
import com.group5.csv.schema.DateTimeSpec;
import com.group5.csv.validation.ValidationError;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for FieldType enum.
 * 
 * Note: Uses mock Field implementation for testing.
 * When FieldSpec (Issue #13) is implemented, consider
 * updating tests to use real Field instances.
 * 
 * @author Edson Ferreira
 */
class FieldTypeTest {

    /**
     * Mock Field implementation for testing FieldType.
     * Provides minimal implementation to support all FieldType operations.
     */
    private static class MockField implements Field {
        private final FieldType type;
        private final DecimalSpec decimalSpec;
        private final DateTimeSpec dateTimeSpec;
        private final DateSpec dateSpec;

        MockField(FieldType type) {
            this.type = type;
            this.decimalSpec = null;
            this.dateTimeSpec = null;
            this.dateSpec = null;
        }

        MockField(FieldType type, DecimalSpec spec) {
            this.type = type;
            this.decimalSpec = spec;
            this.dateTimeSpec = null;
            this.dateSpec = null;
        }

        MockField(FieldType type, DateTimeSpec spec) {
            this.type = type;
            this.decimalSpec = null;
            this.dateTimeSpec = spec;
            this.dateSpec = null;
        }

        MockField(FieldType type, DateSpec spec) {
            this.type = type;
            this.decimalSpec = null;
            this.dateTimeSpec = null;
            this.dateSpec = spec;
        }

        // ---- Field interface implementation ----

        @Override
        public FieldType type() {
            return type;
        }

        @Override
        public DecimalSpec decimalSpec() {
            return decimalSpec;
        }

        @Override
        public DateTimeSpec dateTimeSpec() {
            return dateTimeSpec;
        }

        @Override
        public DateSpec dateSpec() {
            return dateSpec;
        }

        // New methods added to Field – provide simple stubs for testing

        @Override
        public int index() {
            return -1; // "unknown" index in tests
        }

        @Override
        public String name() {
            return "mock";
        }

        @Override
        public String raw() {
            return ""; // not used by these tests
        }

        @Override
        public boolean isMissing() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public java.util.List<ValidationError> errors() {
            return java.util.List.of(); // empty, since we don't model errors here
        }

        @Override
        public Object value() {
            return null; // FieldTypeTest never calls this
        }
    }

    
    // STRING Tests
    
    @Nested
    class StringTypeTests {
        private final Field field = new MockField(FieldType.STRING);
        
        @Test
        void shouldParseValidString() {
            assertEquals("hello", FieldType.STRING.parse("hello", field));
            assertEquals("123", FieldType.STRING.parse("123", field));
        }
        
        @Test
        void shouldHandleNull() {
            assertNull(FieldType.STRING.parse(null, field));
        }
        
        @Test
        void shouldFormatString() {
            assertEquals("hello", FieldType.STRING.format("hello", field));
        }
        
        @Test
        void shouldFormatNull() {
            assertEquals("", FieldType.STRING.format(null, field));
        }
    }
    
    // INT Tests
    
    @Nested
    class IntTypeTests {
        private final Field field = new MockField(FieldType.INT);
        
        @Test
        void shouldParseValidInt() {
            assertEquals(123, FieldType.INT.parse("123", field));
            assertEquals(0, FieldType.INT.parse("0", field));
        }
        
        @Test
        void shouldParseNegativeInt() {
            assertEquals(-456, FieldType.INT.parse("-456", field));
        }
        
        @Test
        void shouldHandleNull() {
            assertNull(FieldType.INT.parse(null, field));
        }
        
        @Test
        void shouldHandleEmpty() {
            assertNull(FieldType.INT.parse("", field));
            assertNull(FieldType.INT.parse("   ", field));
        }
        
        @Test
        void shouldTrimWhitespace() {
            assertEquals(789, FieldType.INT.parse("  789  ", field));
        }
        
        @Test
        void shouldThrowOnInvalidInt() {
            assertThrows(IllegalArgumentException.class, () -> 
                FieldType.INT.parse("abc", field));
            assertThrows(IllegalArgumentException.class, () -> 
                FieldType.INT.parse("12.34", field));
        }
        
        @Test
        void shouldThrowOnOverflow() {
            assertThrows(IllegalArgumentException.class, () -> 
                FieldType.INT.parse("9999999999999", field));
        }
        
        @Test
        void shouldFormatInt() {
            assertEquals("123", FieldType.INT.format(123, field));
            assertEquals("-456", FieldType.INT.format(-456, field));
        }
        
        @Test
        void shouldFormatNull() {
            assertEquals("", FieldType.INT.format(null, field));
        }
    }
    
    // LONG Tests
    
    @Nested
    class LongTypeTests {
        private final Field field = new MockField(FieldType.LONG);
        
        @Test
        void shouldParseValidLong() {
            assertEquals(123456789L, FieldType.LONG.parse("123456789", field));
        }
        
        @Test
        void shouldParseLargeValue() {
            assertEquals(9223372036854775807L, 
                FieldType.LONG.parse("9223372036854775807", field));
        }
        
        @Test
        void shouldParseNegativeLong() {
            assertEquals(-9223372036854775808L, 
                FieldType.LONG.parse("-9223372036854775808", field));
        }
        
        @Test
        void shouldHandleNull() {
            assertNull(FieldType.LONG.parse(null, field));
        }
        
        @Test
        void shouldHandleEmpty() {
            assertNull(FieldType.LONG.parse("", field));
        }
        
        @Test
        void shouldThrowOnInvalidLong() {
            assertThrows(IllegalArgumentException.class, () -> 
                FieldType.LONG.parse("abc", field));
        }
        
        @Test
        void shouldFormatLong() {
            assertEquals("123456789", FieldType.LONG.format(123456789L, field));
        }
        
        @Test
        void shouldFormatNull() {
            assertEquals("", FieldType.LONG.format(null, field));
        }
    }
    
    // DOUBLE Tests
    
    @Nested
    class DoubleTypeTests {
        private final Field field = new MockField(FieldType.DOUBLE);
        
        @Test
        void shouldParseValidDouble() {
            assertEquals(123.456, FieldType.DOUBLE.parse("123.456", field));
        }
        
        @Test
        void shouldParseScientificNotation() {
            assertEquals(1.23e10, FieldType.DOUBLE.parse("1.23e10", field));
        }
        
        @Test
        void shouldParseNegativeDouble() {
            assertEquals(-456.789, FieldType.DOUBLE.parse("-456.789", field));
        }
        
        @Test
        void shouldHandleNull() {
            assertNull(FieldType.DOUBLE.parse(null, field));
        }
        
        @Test
        void shouldHandleEmpty() {
            assertNull(FieldType.DOUBLE.parse("", field));
        }
        
        @Test
        void shouldThrowOnInvalidDouble() {
            assertThrows(IllegalArgumentException.class, () -> 
                FieldType.DOUBLE.parse("abc", field));
        }
        
        @Test
        void shouldFormatDouble() {
            assertEquals("123.456", FieldType.DOUBLE.format(123.456, field));
        }
        
        @Test
        void shouldFormatNull() {
            assertEquals("", FieldType.DOUBLE.format(null, field));
        }
    }
    
    // BOOLEAN Tests
    
    @Nested
    class BooleanTypeTests {
        private final Field field = new MockField(FieldType.BOOLEAN);
        
        @Test
        void shouldParseTrueValues() {
            assertTrue((Boolean) FieldType.BOOLEAN.parse("true", field));
            assertTrue((Boolean) FieldType.BOOLEAN.parse("TRUE", field));
            assertTrue((Boolean) FieldType.BOOLEAN.parse("1", field));
            assertTrue((Boolean) FieldType.BOOLEAN.parse("y", field));
            assertTrue((Boolean) FieldType.BOOLEAN.parse("yes", field));
        }
        
        @Test
        void shouldParseFalseValues() {
            assertFalse((Boolean) FieldType.BOOLEAN.parse("false", field));
            assertFalse((Boolean) FieldType.BOOLEAN.parse("0", field));
            assertFalse((Boolean) FieldType.BOOLEAN.parse("no", field));
            assertFalse((Boolean) FieldType.BOOLEAN.parse("anything", field));
        }
        
        @Test
        void shouldHandleNull() {
            assertNull(FieldType.BOOLEAN.parse(null, field));
        }
        
        @Test
        void shouldTrimWhitespace() {
            assertTrue((Boolean) FieldType.BOOLEAN.parse("  true  ", field));
        }
        
        @Test
        void shouldFormatTrue() {
            assertEquals("true", FieldType.BOOLEAN.format(true, field));
        }
        
        @Test
        void shouldFormatFalse() {
            assertEquals("false", FieldType.BOOLEAN.format(false, field));
        }
        
        @Test
        void shouldFormatNull() {
            assertEquals("", FieldType.BOOLEAN.format(null, field));
        }
    }
    
    // DECIMAL Tests
    
    @Nested
    class DecimalTypeTests {
        private final DecimalSpec spec = DecimalSpec.builder()
            .scale(2)
            .build();
        private final Field field = new MockField(FieldType.DECIMAL, spec);
        
        @Test
        void shouldParseValidDecimal() {
            BigDecimal result = (BigDecimal) FieldType.DECIMAL.parse("123.456", field);
            assertEquals(new BigDecimal("123.46"), result);
        }
        
        @Test
        void shouldHandleNull() {
            assertNull(FieldType.DECIMAL.parse(null, field));
        }
        
        @Test
        void shouldFormatDecimal() {
            String result = FieldType.DECIMAL.format(new BigDecimal("123.45"), field);
            assertEquals("123.45", result);
        }
    }
    
    // DATE Tests
    
    @Nested
    class DateTypeTests {
        private final DateSpec spec = new DateSpec(
            new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE", "EU_DMY", "US_MDY")
                .allowBlank(true)  // Allow null/blank
                .build()
        );
        private final Field field = new MockField(FieldType.DATE, spec);
        
        @Test
        void shouldParseValidDate() {
            LocalDate result = (LocalDate) FieldType.DATE.parse("2025-11-15", field);
            assertEquals(LocalDate.of(2025, 11, 15), result);
        }
        
        @Test
        void shouldHandleNull() {
            assertNull(FieldType.DATE.parse(null, field));
        }
        
        @Test
        void shouldFormatDate() {
            String result = FieldType.DATE.format(LocalDate.of(2025, 11, 15), field);
            assertNotNull(result);
            assertTrue(result.contains("2025"));
        }
        
        @Test
        void shouldFormatNull() {
            assertEquals("", FieldType.DATE.format(null, field));
        }
    }
    
    // DATETIME Tests
    
    @Nested
    class DateTimeTypeTests {
        private final DateTimeSpec spec = new DateTimeSpec.Builder()
            .acceptPresets("ISO_LOCAL_DATE_TIME")
            .allowBlank(true)  // Allow null/blank
            .build();
        private final Field field = new MockField(FieldType.DATETIME, spec);
        
        @Test
        void shouldParseValidDateTime() {
            LocalDateTime result = (LocalDateTime) FieldType.DATETIME.parse("2025-11-15T10:30:00", field);
            assertEquals(LocalDateTime.of(2025, 11, 15, 10, 30, 0), result);
        }
        
        @Test
        void shouldHandleNull() {
            assertNull(FieldType.DATETIME.parse(null, field));
        }
        
        @Test
        void shouldFormatDateTime() {
            String result = FieldType.DATETIME.format(
                LocalDateTime.of(2025, 11, 15, 10, 30, 0), field);
            assertNotNull(result);
        }
        
        @Test
        void shouldFormatNull() {
            assertEquals("", FieldType.DATETIME.format(null, field));
        }
    }
}
