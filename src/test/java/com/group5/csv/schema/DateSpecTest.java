package com.group5.csv.schema;

import com.group5.csv.exceptions.ParseException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateSpecTest {

    @Test
    void parsesIsoDate() {
        DateSpec spec = new DateSpec();
        LocalDate d = spec.parse("2025-11-09");
        assertEquals(LocalDate.of(2025, 11, 9), d);
    }

    @Test
    void parsesEuropeanFormat() {
        DateSpec spec = new DateSpec();
        LocalDate d = spec.parse("31/12/2025");
        assertEquals(LocalDate.of(2025, 12, 31), d);
    }

    @Test
    void rejectsInvalidDate() {
        DateSpec spec = new DateSpec();
        assertThrows(ParseException.class, () -> spec.parse("99/99/9999"));
    }

    @Test
    void formatsDateToString() {
        DateSpec spec = new DateSpec();
        LocalDate input = LocalDate.of(2025, 11, 9);
        String out = spec.format(input);
        assertNotNull(out);
        assertFalse(out.isBlank());
    }

    @Test
    void returnsNullForBlankWhenAllowed() {
        DateTimeSpec base = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE")
                .allowBlank(true)
                .build();
        DateSpec spec = new DateSpec(base);
        assertNull(spec.parse(""));
    }
}
