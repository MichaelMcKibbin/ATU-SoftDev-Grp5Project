package com.group5.csv.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvWarningTest {

    @Test
    void testFieldsAndAccessors() {
        CsvWarning w = new CsvWarning(
                12,
                CsvWarning.Type.TOO_FEW_FIELDS,
                "missing 2 fields"
        );

        assertEquals(12, w.line());
        assertEquals(CsvWarning.Type.TOO_FEW_FIELDS, w.type());
        assertEquals("missing 2 fields", w.message());
    }

    @Test
    void testToStringContainsAllParts() {
        CsvWarning w = new CsvWarning(
                5,
                CsvWarning.Type.TOO_MANY_FIELDS,
                "2 extra fields"
        );

        String s = w.toString().toLowerCase();

        assertTrue(s.contains("line=5"), "toString must contain correct line");
        assertTrue(s.contains("too_many_fields"), "toString must contain enum name");
        assertTrue(s.contains("2 extra fields"), "toString must contain message");
        assertTrue(s.contains("csvwarning"), "toString must contain class name");
    }
}