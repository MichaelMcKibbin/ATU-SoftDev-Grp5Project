package com.group5.csv.schema;

import com.group5.csv.exceptions.ParseException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeSpecTest {

    @Test
    void parsesFullDateTimeWithCustomPattern() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPatterns("uuuu-MM-dd HH:mm:ss")
                .build();

        LocalDateTime dt = spec.parse("2024-11-09 15:30:00");
        assertEquals(2024, dt.getYear());
        assertEquals(11, dt.getMonthValue());
        assertEquals(9, dt.getDayOfMonth());
        assertEquals(15, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(0, dt.getSecond());
    }

    @Test
    void parsesIsoOffsetAndRfc1123Presets() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_OFFSET_DATE_TIME", "RFC_1123_DATE_TIME")
                .zone(ZoneId.of("Europe/Dublin"))
                .build();

        // ISO_OFFSET_DATE_TIME
        LocalDateTime a = spec.parse("2025-01-02T03:04:05+01:00");
        assertEquals(2025, a.getYear());
        assertEquals(1, a.getMonthValue());
        assertEquals(2, a.getDayOfMonth());

        // RFC_1123_DATE_TIME
        LocalDateTime b = spec.parse("Sun, 02 Feb 2025 14:30:00 GMT");
        assertEquals(2025, b.getYear());
        assertEquals(2, b.getMonthValue());
        assertEquals(2, b.getDayOfMonth());
        assertEquals(14, b.getHour());
        assertEquals(30, b.getMinute());
    }

    @Test
    void dateOnlyDefaultsToMidnight() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE", "EU_DMY")
                .missingPartPolicy(DateTimeSpec.MissingPartPolicy.DEFAULTS)
                .build();

        LocalDateTime dt1 = spec.parse("2025-12-31");
        assertEquals(0, dt1.getHour());
        assertEquals(0, dt1.getMinute());

        LocalDateTime dt2 = spec.parse("31/12/2025");
        assertEquals(0, dt2.getHour());
        assertEquals(0, dt2.getMinute());
    }

    @Test
    void timeOnlyDefaultsToEpochDate() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_TIME")
                .acceptPatterns("HH:mm", "hh:mm a")
                .missingPartPolicy(DateTimeSpec.MissingPartPolicy.DEFAULTS)
                .strict(false) // allow minor variations
                .build();

        LocalDateTime t1 = spec.parse("23:59");
        assertEquals(1970, t1.getYear());
        assertEquals(1, t1.getMonthValue());
        assertEquals(1, t1.getDayOfMonth());
        assertEquals(23, t1.getHour());
        assertEquals(59, t1.getMinute());

        LocalDateTime t2 = spec.parse("11:45 PM");
        assertEquals(23, t2.getHour());
        assertEquals(45, t2.getMinute());
    }

    @Test
    void lenientSeparatorNormalizationWhenNotStrict() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPatterns("uuuu-MM-dd HH:mm:ss")
                .strict(false) // enables small normalization like '/' -> '-'
                .build();

        LocalDateTime dt = spec.parse("2025/03/01 08:09:10"); // slashes instead of dashes
        assertEquals(2025, dt.getYear());
        assertEquals(3, dt.getMonthValue());
        assertEquals(1, dt.getDayOfMonth());
        assertEquals(8, dt.getHour());
        assertEquals(9, dt.getMinute());
        assertEquals(10, dt.getSecond());
    }

    @Test
    void blankHandlingReturnsNullWhenAllowed() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE_TIME")
                .allowBlank(true)
                .build();

        assertNull(spec.parse("   "));
        assertTrue(spec.isValid("")); // treated as valid when allowBlank = true
    }

    @Test
    void rejectsInvalidInputs() {
        DateTimeSpec spec = new DateTimeSpec.Builder()
                .acceptPatterns("uuuu-MM-dd HH:mm:ss")
                .build();

        assertThrows(ParseException.class, () -> spec.parse("not-a-date"));
        assertThrows(ParseException.class, () -> spec.parse("31-31-2025 99:99:99"));
    }
}
