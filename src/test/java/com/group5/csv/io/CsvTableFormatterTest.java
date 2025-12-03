package com.group5.csv.io;

import com.group5.csv.core.Row;
import com.group5.csv.testutils.VirtualReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CsvTableFormatter
 */
class CsvTableFormatterTest {

    /**
     * Helper to read all rows from a CsvReader.
     */
    private List<Row> readAll(CsvReader reader) throws Exception {
        List<Row> rows = new ArrayList<>();
        Row row;
        while ((row = reader.readRow()) != null) {
            rows.add(row);
        }
        return rows;
    }

    @Test
    void formatTable_withHeadersAndRows_producesBorderedTable() throws Exception {
        String csv = """
                name,age,city
                Alice,30,Dublin
                Bob,25,Cork
                """;

        CsvConfig config = CsvConfig.builder()
                .setHasHeader(true)
                .build();

        String table;
        try (CsvReader reader = new CsvReader(new VirtualReader(csv), config)) {
            List<Row> rows = readAll(reader);

            // Use the reader-based constructor so formatter picks up the same newline
            CsvTableFormatter formatter = new CsvTableFormatter(reader);

            table = formatter.formatTable(rows);
        }

        assertNotNull(table);
        assertFalse(table.isEmpty(), "Table output should not be empty");

        // Basic structure checks
        assertTrue(table.startsWith("+"), "Table should start with a separator line");
        assertTrue(table.trim().endsWith("+"), "Table should end with a separator line");
        assertTrue(table.contains("|"), "Table should use '|' as column borders");

        // Headers present
        assertTrue(table.contains("name"), "Header 'name' should be present");
        assertTrue(table.contains("age"), "Header 'age' should be present");
        assertTrue(table.contains("city"), "Header 'city' should be present");

        // Data present
        assertTrue(table.contains("Alice"), "Data row should contain 'Alice'");
        assertTrue(table.contains("Bob"), "Data row should contain 'Bob'");
    }

    @Test
    void formatTable_emptyList_returnsEmptyString() {
        CsvTableFormatter formatter = new CsvTableFormatter("\n");
        String table = formatter.formatTable(List.of());

        assertNotNull(table);
        assertEquals("", table, "Empty input should produce an empty string");
    }

    @Test
    void formatRow_multilineCell_producesMultipleLinesWithBorders() throws Exception {
        String csv = """
            name,comment
            Alice,"Hello
            World"
            """;

        CsvConfig config = CsvConfig.builder()
                .setHasHeader(true)
                .build();

        String formatted;
        try (CsvReader reader = new CsvReader(new VirtualReader(csv), config)) {
            // Directly read all data rows (header is handled inside CsvReader)
            List<Row> rows = readAll(reader);
            assertEquals(1, rows.size(), "Expected one data row");

            CsvTableFormatter formatter = new CsvTableFormatter(reader);
            formatted = formatter.formatRow(rows.get(0));
        }

        assertNotNull(formatted);

        // It should contain both lines of the multi-line cell
        assertTrue(formatted.contains("Hello"), "First line of cell should appear");
        assertTrue(formatted.contains("World"), "Second line of cell should appear");

        // Split on line breaks and check borders
        String[] lines = formatted.split("\\R");
        assertTrue(lines.length >= 2, "Multiline cell should produce multiple lines");

        for (String line : lines) {
            assertTrue(line.startsWith("|"), "Each formatted line should start with '|'");
            assertTrue(line.endsWith("|"), "Each formatted line should end with '|'");
        }
    }
}
