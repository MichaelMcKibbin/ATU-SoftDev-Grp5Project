package com.group5.csv.schema;

import com.group5.csv.exceptions.ParseException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeSpecTest {

    @Test
    void parsesIsoLocalDateTime() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE_TIME")
                .build();

        LocalDateTime dt = spec.parse("2025-11-30T14:30:45");
        assertEquals(LocalDateTime.of(2025, 11, 30, 14, 30, 45), dt);
    }

    @Test
    void parsesCustomPattern() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPatterns("dd/MM/yyyy HH:mm")
                .build();

        LocalDateTime dt = spec.parse("30/11/2025 14:30");
        assertEquals(LocalDateTime.of(2025, 11, 30, 14, 30), dt);
    }

    @Test
    void throwsOnInvalidDateTime() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE_TIME")
                .build();

        assertThrows(ParseException.class, () -> spec.parse("2025-13-45T25:70:90"));
    }

    @Test
    void allowsBlankWhenConfigured() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE_TIME")
                .allowBlank(true)
                .build();

        assertNull(spec.parse(""));
        assertNull(spec.parse("   "));
    }

    @Test
    void rejectsBlankWhenNotAllowed() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE_TIME")
                .allowBlank(false)
                .build();

        assertThrows(ParseException.class, () -> spec.parse(""));
    }

    @Test
    void formatsDateTimeToString() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE_TIME")
                .build();

        LocalDateTime input = LocalDateTime.of(2025, 11, 30, 14, 30, 45);
        String formatted = spec.format(input);

        assertNotNull(formatted);
        assertFalse(formatted.isBlank());
    }

    @Test
    void isValidReturnsTrueForValidDateTime() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE_TIME")
                .build();

        assertTrue(spec.isValid("2025-11-30T14:30:45"));
    }

    @Test
    void isValidReturnsFalseForInvalidDateTime() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE_TIME")
                .build();

        assertFalse(spec.isValid("invalid"));
        assertFalse(spec.isValid("2025-13-45T25:70:90"));
    }

    @Test
    void formatNullReturnsNull() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE_TIME")
                .build();

        assertNull(spec.format(null));
    }
}
