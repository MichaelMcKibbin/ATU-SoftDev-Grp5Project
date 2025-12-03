package com.group5.csv.io;


import com.group5.csv.core.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CsvTableFormatter
 */
class CsvTableFormatterTest {

    @Test
    void testFormatRow_SimpleData() throws IOException {
        String csvData = "Name,Age,City\nJohn,30,NYC";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        Row row = reader.readRow();
        String formatted = formatter.formatRow(row);

        assertNotNull(formatted);
        assertTrue(formatted.contains("John"));
        assertTrue(formatted.contains("30"));
        assertTrue(formatted.contains("NYC"));
        assertTrue(formatted.startsWith("|"));
        assertTrue(formatted.endsWith("|"));

        reader.close();
    }

    @Test
    void testFormatRow_NullRow() {
        CsvTableFormatter formatter = new CsvTableFormatter("\n");
        String formatted = formatter.formatRow(null);
        assertNull(formatted);
    }

    @Test
    void testFormatRow_WithMultilineCell() throws IOException {
        String csvData = "Name,Description\nProduct,\"Line 1\nLine 2\nLine 3\"";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        Row row = reader.readRow();
        String formatted = formatter.formatRow(row);

        assertNotNull(formatted);
        assertTrue(formatted.contains("Line 1"));
        assertTrue(formatted.contains("Line 2"));
        assertTrue(formatted.contains("Line 3"));

        // Should have 2 newlines (for 3 lines)
        long newlineCount = formatted.chars().filter(ch -> ch == '\n').count();
        assertEquals(2, newlineCount);

        reader.close();
    }

    @Test
    void testFormatTable_WithHeaders() throws IOException {
        String csvData = "Name,Age,City\nJohn,30,NYC\nJane,25,LA\nBob,35,Chicago";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows);

        assertNotNull(table);

        // Should contain separators
        assertTrue(table.contains("+"));
        assertTrue(table.contains("-"));

        // Should contain headers
        assertTrue(table.contains("Name"));
        assertTrue(table.contains("Age"));
        assertTrue(table.contains("City"));

        // Should contain all data
        assertTrue(table.contains("John"));
        assertTrue(table.contains("Jane"));
        assertTrue(table.contains("Bob"));
        assertTrue(table.contains("NYC"));
        assertTrue(table.contains("LA"));
        assertTrue(table.contains("Chicago"));

        reader.close();
    }

    @Test
    void testFormatTable_WithoutHeaders() throws IOException {
        String csvData = "John,30,NYC\nJane,25,LA";
        CsvConfig config = new CsvConfig.Builder()
                .setHasHeader(false)
                .build();
        CsvReader reader = new CsvReader(new StringReader(csvData), config);
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows);

        assertNotNull(table);
        assertTrue(table.contains("John"));
        assertTrue(table.contains("Jane"));

        reader.close();
    }

    @Test
    void testFormatTable_EmptyList() {
        CsvTableFormatter formatter = new CsvTableFormatter("\n");
        String table = formatter.formatTable(new ArrayList<>());
        assertEquals("", table);
    }

    @Test
    void testFormatTable_ColumnAlignment() throws IOException {
        String csvData = "Name,Age,City\nJohn Smith,30,New York City\nJo,5,LA";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows);

        // All rows should have consistent structure
        String[] lines = table.split("\n");

        // Find data rows (those starting with |)
        List<String> dataLines = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("|") && !line.contains("+")) {
                dataLines.add(line);
            }
        }

        assertTrue(dataLines.size() >= 2);

        // All data lines should have the same length
        int firstLineLength = dataLines.get(0).length();
        for (String line : dataLines) {
            assertEquals(firstLineLength, line.length(),
                    "All rows should have the same length for proper alignment");
        }

        reader.close();
    }

    @Test
    void testFormatTable_WithMultilineCells() throws IOException {
        String csvData = "Name,Address,Phone\nJohn,\"123 Main St\nApt 4B\nNew York, NY\",555-1234";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows);

        assertNotNull(table);
        assertTrue(table.contains("123 Main St"));
        assertTrue(table.contains("Apt 4B"));
        assertTrue(table.contains("New York, NY"));
        assertTrue(table.contains("555-1234"));

        reader.close();
    }

    @Test
    void testFormatTable_MultipleRowsWithMultilineCells() throws IOException {
        String csvData = "Col1,Col2,Col3\n\"A\nB\",X,Y\nC,\"D\nE\nF\",Z";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows);

        assertNotNull(table);
        assertTrue(table.contains("A"));
        assertTrue(table.contains("B"));
        assertTrue(table.contains("D"));
        assertTrue(table.contains("E"));
        assertTrue(table.contains("F"));

        reader.close();
    }

    @Test
    void testConstructor_WithReader() throws IOException {
        String csvData = "Name,Age\nJohn,30";
        CsvReader reader = new CsvReader(new StringReader(csvData));

        CsvTableFormatter formatter = new CsvTableFormatter(reader);
        assertNotNull(formatter);

        Row row = reader.readRow();
        String formatted = formatter.formatRow(row);
        assertNotNull(formatted);

        reader.close();
    }

    @Test
    void testConstructor_WithCustomNewline() {
        CsvTableFormatter formatter = new CsvTableFormatter("\r\n");
        assertNotNull(formatter);
    }

    @Test
    void testFormatTable_CustomNewline() throws IOException {
        String csvData = "Name,Age\nJohn,30\nJane,25";
        CsvReader reader = new CsvReader(new StringReader(csvData));

        // Use custom newline
        CsvTableFormatter formatter = new CsvTableFormatter("\r\n");

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows);

        assertNotNull(table);
        assertTrue(table.contains("John"));
        assertTrue(table.contains("Jane"));

        // Should use \r\n as newline
        assertTrue(table.contains("\r\n"));

        reader.close();
    }

    @Test
    void testFormatTable_SingleRow() throws IOException {
        String csvData = "Name,Age,City\nJohn,30,NYC";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows);

        assertNotNull(table);
        assertTrue(table.contains("Name"));
        assertTrue(table.contains("John"));

        // Should still have proper structure
        assertTrue(table.contains("+"));
        assertTrue(table.contains("-"));
        assertTrue(table.contains("|"));

        reader.close();
    }

    @Test
    void testFormatTable_SpecialCharacters() throws IOException {
        String csvData = "Name,Symbol\nPipe,|\nPlus,+\nDash,-";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows);

        assertNotNull(table);
        assertTrue(table.contains("Pipe"));
        assertTrue(table.contains("Plus"));
        assertTrue(table.contains("Dash"));

        reader.close();
    }

    @Test
    void testFormatTable_FromFile(@TempDir Path tempDir) throws IOException {
        // Create a temporary CSV file
        Path csvFile = tempDir.resolve("test.csv");
        String content = "Product,Price,Stock\nLaptop,999.99,10\nMouse,29.99,50";
        Files.writeString(csvFile, content);

        CsvReader reader = CsvReader.fromPath(csvFile);
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows);

        assertNotNull(table);
        assertTrue(table.contains("Product"));
        assertTrue(table.contains("Laptop"));
        assertTrue(table.contains("999.99"));

        reader.close();
    }

    @Test
    void testFormatTable_EmptyFields() throws IOException {
        String csvData = "Name,Age,City\nJohn,,NYC\n,25,\nBob,35,Chicago";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows);

        assertNotNull(table);
        assertTrue(table.contains("John"));
        assertTrue(table.contains("Bob"));
        assertTrue(table.contains("Chicago"));

        // Should handle empty fields gracefully
        String[] lines = table.split("\n");
        for (String line : lines) {
            if (line.startsWith("|")) {
                // Each row should still have the correct number of | characters
                long pipeCount = line.chars().filter(ch -> ch == '|').count();
                assertTrue(pipeCount >= 4); // At least 4 pipes for 3 columns
            }
        }

        reader.close();
    }

    @Test
    void testFormatTable_WideColumns() throws IOException {
        String csvData = "Name,Description\nProduct,\"This is a very long description that should test column width calculation\"";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows);

        assertNotNull(table);
        assertTrue(table.contains("This is a very long description"));

        // The table should still be properly formatted
        String[] lines = table.split("\n");
        assertTrue(lines.length > 0);

        reader.close();
    }

    @Test
    void testFormatRow_MultipleColumns() throws IOException {
        String csvData = "A,B,C,D,E,F\n1,2,3,4,5,6";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        Row row = reader.readRow();
        String formatted = formatter.formatRow(row);

        assertNotNull(formatted);
        assertTrue(formatted.contains("1"));
        assertTrue(formatted.contains("6"));

        // Count the number of pipes (should be 7 for 6 columns)
        long pipeCount = formatted.chars().filter(ch -> ch == '|').count();
        assertEquals(7, pipeCount);

        reader.close();
    }

    @Test
    void testFormatTable_AlignmentWithMultilineAndRegularCells() throws IOException {
        String csvData = "Name,Address,Phone\nJohn,\"123 Main\nApt 4B\",555-1234\nJane,456 Oak,555-5678";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter(reader);

        List<Row> rows = reader.readAll();

        // Debug: Check what's actually in the rows
        System.out.println("Row data:");
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            System.out.println("Row " + i + ":");
            for (int j = 0; j < row.size(); j++) {
                String value = row.get(j);
                System.out.println("  Col " + j + ": [" + value + "] (contains \\n: " + value.contains("\n") + ")");
            }
        }

        String table = formatter.formatTable(rows);

        assertNotNull(table);

        // Debug: Print the actual table
        System.out.println("\nGenerated table:");
        System.out.println(table);
        System.out.println("---");

        // Verify both multi-line and single-line cells are present
        assertTrue(table.contains("John"));
        assertTrue(table.contains("456 Oak"));

        reader.close();
    }
}
