package com.group5.csv.schema;

import com.group5.csv.exceptions.ParseException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeSpecTest {

    @Test
    void parses24HourTime() {
        TimeSpec spec = new TimeSpec();
        LocalTime t = spec.parse("23:59");
        assertEquals(23, t.getHour());
        assertEquals(59, t.getMinute());
    }

    @Test
    void parses12HourTimeWithAmPm() {
        TimeSpec spec = new TimeSpec();
        LocalTime t = spec.parse("11:45 PM");
        assertEquals(23, t.getHour());
        assertEquals(45, t.getMinute());
    }

    @Test
    void rejectsInvalidTime() {
        TimeSpec spec = new TimeSpec();
        assertThrows(ParseException.class, () -> spec.parse("25:99"));
    }

    @Test
    void formatsTimeToString() {
        TimeSpec spec = new TimeSpec();
        LocalTime input = LocalTime.of(14, 30);
        String out = spec.format(input);
        assertNotNull(out);
        assertFalse(out.isBlank());
    }

    @Test
    void returnsNullForBlankWhenAllowed() {
        DateTimeSpec base = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_TIME")
                .allowBlank(true)
                .build();
        TimeSpec spec = new TimeSpec(base);
        assertNull(spec.parse(""));
    }
}
