package com.group5.csv.schema;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class DecimalSpecTest {

    @Test
    void defaultBuilderHasExpectedSettings() {
        DecimalSpec spec = DecimalSpec.builder().build();

        assertEquals(2, spec.scale());
        assertNull(spec.precision());
        assertEquals(RoundingMode.HALF_UP, spec.rounding());
        assertFalse(spec.allowBlank());
        assertNull(spec.min());
        assertNull(spec.max());
    }

    // ------- parse() happy-path tests -------

    @Test
    void parseSimpleValueWithDefaultScale() {
        DecimalSpec spec = DecimalSpec.builder().build();

        BigDecimal result = spec.parse("123.45");

        assertEquals(new BigDecimal("123.45"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void parseRoundsHalfUpToScale() {
        DecimalSpec spec = DecimalSpec.builder()
                .scale(2)
                .rounding(RoundingMode.HALF_UP)
                .build();

        // 1.234 -> 1.23
        assertEquals(new BigDecimal("1.23"), spec.parse("1.234"));

        // 1.235 -> 1.24
        assertEquals(new BigDecimal("1.24"), spec.parse("1.235"));
    }

    @Test
    void parseTrimsWhitespace() {
        DecimalSpec spec = DecimalSpec.builder().build();

        BigDecimal result = spec.parse("   10.50   ");

        assertEquals(new BigDecimal("10.50"), result);
    }

    @Test
    void parseNullReturnsNullRegardlessOfAllowBlank() {
        DecimalSpec strict = DecimalSpec.builder().allowBlank(false).build();
        DecimalSpec lenient = DecimalSpec.builder().allowBlank(true).build();

        assertNull(strict.parse(null));
        assertNull(lenient.parse(null));
    }

    // ------- parse() blank-handling -------

    @Test
    void parseBlankThrowsWhenAllowBlankFalse() {
        DecimalSpec spec = DecimalSpec.builder()
                .allowBlank(false)
                .build();

        assertThrows(IllegalArgumentException.class, () -> spec.parse(""));
        assertThrows(IllegalArgumentException.class, () -> spec.parse("   "));
    }

    @Test
    void parseBlankReturnsNullWhenAllowBlankTrue() {
        DecimalSpec spec = DecimalSpec.builder()
                .allowBlank(true)
                .build();

        assertNull(spec.parse(""));
        assertNull(spec.parse("   "));
    }

    // ------- precision & range checks -------

    @Test
    void parseThrowsOnPrecisionOverflow() {
        DecimalSpec spec = DecimalSpec.builder()
                .precision(4)  // max 4 digits total
                .build();

        // "123.4" -> unscaled 1234 -> precision 4 (OK)
        assertEquals(new BigDecimal("123.40"), spec.parse("123.4"));

        // "123.45" -> unscaled 12345 -> precision 5 (overflow)
        assertThrows(IllegalArgumentException.class, () -> spec.parse("123.45"));
    }

    @Test
    void parseThrowsWhenBelowMin() {
        DecimalSpec spec = DecimalSpec.builder()
                .min(new BigDecimal("0.00"))
                .build();

        // Exactly at min – OK
        assertEquals(new BigDecimal("0.00"), spec.parse("0"));

        // Below min – should throw
        assertThrows(IllegalArgumentException.class, () -> spec.parse("-0.01"));
    }

    @Test
    void parseThrowsWhenAboveMax() {
        DecimalSpec spec = DecimalSpec.builder()
                .max(new BigDecimal("100.00"))
                .build();

        // Exactly at max – OK
        assertEquals(new BigDecimal("100.00"), spec.parse("100"));

        // Above max – should throw
        assertThrows(IllegalArgumentException.class, () -> spec.parse("100.01"));
    }

    // ------- format() tests -------

    @Test
    void formatScalesAndUsesPlainString() {
        DecimalSpec spec = DecimalSpec.builder()
                .scale(2)
                .rounding(RoundingMode.HALF_UP)
                .build();

        String formatted = spec.format(new BigDecimal("1.234"));

        assertEquals("1.23", formatted);
    }

    @Test
    void formatNullWithAllowBlankTrueReturnsEmptyString() {
        DecimalSpec spec = DecimalSpec.builder()
                .allowBlank(true)
                .build();

        assertEquals("", spec.format(null));
    }

    @Test
    void formatNullWithAllowBlankFalseCurrentlyReturnsEmptyString() {
        // Note: current implementation uses "0".repeat(0) => ""
        // This test documents that behaviour as-is.
        DecimalSpec spec = DecimalSpec.builder()
                .allowBlank(false)
                .build();

        assertEquals("", spec.format(null));
    }

    // ------- builder / getters integration -------

    @Test
    void builderSetsAllPropertiesCorrectly() {
        DecimalSpec spec = DecimalSpec.builder()
                .scale(3)
                .precision(6)
                .rounding(RoundingMode.DOWN)
                .allowBlank(true)
                .min(new BigDecimal("1.000"))
                .max(new BigDecimal("999.999"))
                .build();

        assertEquals(3, spec.scale());
        assertEquals(6, spec.precision());
        assertEquals(RoundingMode.DOWN, spec.rounding());
        assertTrue(spec.allowBlank());
        assertEquals(new BigDecimal("1.000"), spec.min());
        assertEquals(new BigDecimal("999.999"), spec.max());
    }
}
