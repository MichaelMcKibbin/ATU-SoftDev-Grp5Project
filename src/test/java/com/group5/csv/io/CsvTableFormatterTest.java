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
    void testFormatTable_SimpleData() throws IOException {
        String csvData = "Name,Age,City\nJohn,30,NYC";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

        assertNotNull(table);
        assertTrue(table.contains("John"));
        assertTrue(table.contains("30"));
        assertTrue(table.contains("NYC"));
        assertTrue(table.contains("|"));

        reader.close();
    }

    @Test
    void testFormatTable_NullList() {
        CsvTableFormatter formatter = new CsvTableFormatter("\n");
        String formatted = formatter.formatTable(null, -1);
        assertEquals("", formatted);
    }

    @Test
    void testFormatTable_WithMultilineCell() throws IOException {
        String csvData = "Name,Description\nProduct,\"Line 1\nLine 2\nLine 3\"";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

        assertNotNull(table);
        assertTrue(table.contains("Line 1"));
        assertTrue(table.contains("Line 2"));
        assertTrue(table.contains("Line 3"));

        reader.close();
    }

    @Test
    void testFormatTable_WithHeaders() throws IOException {
        String csvData = "Name,Age,City\nJohn,30,NYC\nJane,25,LA\nBob,35,Chicago";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

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
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

        assertNotNull(table);
        assertTrue(table.contains("John"));
        assertTrue(table.contains("Jane"));

        reader.close();
    }

    @Test
    void testFormatTable_EmptyList() {
        CsvTableFormatter formatter = new CsvTableFormatter("\n");
        String table = formatter.formatTable(new ArrayList<>(), -1);
        assertEquals("", table);
    }

    @Test
    void testFormatTable_ColumnAlignment() throws IOException {
        String csvData = "Name,Age,City\nJohn Smith,30,New York City\nJo,5,LA";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

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
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

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
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

        assertNotNull(table);
        assertTrue(table.contains("A"));
        assertTrue(table.contains("B"));
        assertTrue(table.contains("D"));
        assertTrue(table.contains("E"));
        assertTrue(table.contains("F"));

        reader.close();
    }

    @Test
    void testConstructor_DefaultNewline() {
        CsvTableFormatter formatter = new CsvTableFormatter();
        assertNotNull(formatter);
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
        String table = formatter.formatTable(rows, -1);

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
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

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
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

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
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

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
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

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
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

        assertNotNull(table);
        assertTrue(table.contains("This is a very long description"));

        // The table should still be properly formatted
        String[] lines = table.split("\n");
        assertTrue(lines.length > 0);

        reader.close();
    }

    @Test
    void testFormatTable_WithLimit() throws IOException {
        String csvData = "Name,Age\nJohn,30\nJane,25\nBob,35\nAlice,28";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, 2);

        assertNotNull(table);
        assertTrue(table.contains("John"));
        assertTrue(table.contains("Jane"));
        assertFalse(table.contains("Bob"));
        assertFalse(table.contains("Alice"));

        reader.close();
    }

    @Test
    void testFormatTable_AlignmentWithMultilineAndRegularCells() throws IOException {
        String csvData = "Name,Address,Phone\nJohn,\"123 Main\nApt 4B\",555-1234\nJane,456 Oak,555-5678";
        CsvReader reader = new CsvReader(new StringReader(csvData));
        CsvTableFormatter formatter = new CsvTableFormatter();

        List<Row> rows = reader.readAll();
        String table = formatter.formatTable(rows, -1);

        assertNotNull(table);

        // Verify both multi-line and single-line cells are present
        assertTrue(table.contains("John"));
        assertTrue(table.contains("456 Oak"));

        reader.close();
    }
}
