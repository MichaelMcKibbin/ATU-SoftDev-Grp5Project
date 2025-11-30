package com.group5.csv.schema;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeSpecTest {

    @Test
    void parsesIsoLocalTime() {
        TimeSpec spec = new TimeSpec();
        LocalTime t = spec.parse("14:30:45");
        assertEquals(LocalTime.of(14, 30, 45), t);
    }

    @Test
    void parsesHhMmFormat() {
        TimeSpec spec = new TimeSpec();
        LocalTime t = spec.parse("14:30");
        assertEquals(LocalTime.of(14, 30), t);
    }

    @Test
    void parses12HourFormatWithAm() {
        TimeSpec spec = new TimeSpec();
        LocalTime t = spec.parse("09:15 AM");
        assertEquals(LocalTime.of(9, 15), t);
    }

    @Test
    void parses12HourFormatWithPm() {
        TimeSpec spec = new TimeSpec();
        LocalTime t = spec.parse("02:45 PM");
        assertEquals(LocalTime.of(14, 45), t);
    }

    @Test
    void formatsTimeToString() {
        TimeSpec spec = new TimeSpec();
        LocalTime input = LocalTime.of(14, 30, 45);
        String out = spec.format(input);
        assertNotNull(out);
        assertFalse(out.isBlank());
    }

    @Test
    void isValidReturnsTrueForValidTime() {
        TimeSpec spec = new TimeSpec();
        assertTrue(spec.isValid("14:30:45"));
        assertTrue(spec.isValid("09:15 AM"));
    }

    @Test
    void isValidReturnsFalseForInvalidTime() {
        TimeSpec spec = new TimeSpec();
        assertFalse(spec.isValid("25:00:00"));
        assertFalse(spec.isValid("invalid"));
    }

    @Test
    void formatNullReturnsNull() {
        TimeSpec spec = new TimeSpec();
        assertNull(spec.format(null));
    }
}
