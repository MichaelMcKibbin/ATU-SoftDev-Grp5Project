package com.group5.csv.io;

import com.group5.csv.core.Row;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Round-trip tests for CsvReader and CsvWriter.
 * <p>
 * Ensures that data can be:
 * CSV -> Reader -> Writer -> Reader
 * without loss or corruption.
 */
public class CsvRoundTripTest {

    /**
     * Default round-trip test using standard CSV configuration.
     */
    @Test
    public void testDefaultConfig() throws Exception {
        List<String> data = List.of(
                "name,age,city",
                "John,25,Dublin",
                "Anna,30,\"New York\"",
                "\"Smith, Bob\",40,Paris"
        );

        runRoundTripTest(data);
    }

    /**
     * Round-trip test using empty lines.
     */
    @Test
    public void testEmptyLines() throws Exception {
        List<String> data = List.of(
                "name,age,city",
                "",
                "",
                "John,25,Dublin",
                "",
                "Anna,30,\"New York\"",
                "\"Smith, Bob\",40,Paris"
        );

        CsvConfig config = CsvConfig.builder()
                .setSkipEmptyLines(false)
                .build();

        runRoundTripTest(data, config);
    }

    /**
     * Verifies that escaped quotes inside quoted fields are preserved correctly.
     */
    @Test
    public void testEscapedQuotes() throws Exception {
        List<String> data = List.of(
                "text",
                "\"He said \"\"Hello\"\" to her\"",
                "\"\"\"Start and end quotes\"\"\""
        );

        runRoundTripTest(data);
    }

    /**
     * Verifies that quoted fields spanning multiple lines are handled correctly.
     */
    @Test
    public void testMultilineField() throws Exception {
        List<String> data = List.of(
                "id,comment",
                "1,\"Hello",
                "world\"",
                "2,Single line"
        );

        CsvConfig config = CsvConfig.builder()
                .setHasHeader(true)
                .build();

        runRoundTripTest(data, config, 2);
    }

    /**
     * Verifies that leading and trailing whitespace is preserved.
     */
    @Test
    public void testWhitespacePreservation() throws Exception {
        List<String> data = List.of(
                "value",
                "  leading",
                "trailing  ",
                "  both  "
        );

        runRoundTripTest(data);
    }

    /**
     * Verifies correct handling of empty and missing columns.
     */
    @Test
    public void testEmptyColumns() throws Exception {
        List<String> data = List.of(
                "a,b,c,d",
                "1,,3,",
                ",2,,4"
        );

        runRoundTripTest(data);
    }

    /**
     * Round-trip test using semicolons as delimiter.
     */
    @Test
    public void testSemicolonDelimiter() throws Exception {
        List<String> data = List.of(
                "name;age;city",
                "John;25;Dublin"
        );

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.excel_semicolon())
                .build();

        runRoundTripTest(data, config);
    }

    /**
     * Round-trip test using tab as delimiter (TSV format).
     */
    @Test
    public void testTSVFormat() throws Exception {
        List<String> data = List.of(
                "name\tage\tcity",
                "John\t25\tDublin"
        );

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.tsv())
                .build();

        runRoundTripTest(data, config);
    }

    /**
     * Round-trip test with headers enabled.
     */
    @Test
    public void testWithHeadersEnabled() throws Exception {
        List<String> data = List.of(
                "id,name,score",
                "1,Alice,95",
                "2,Bob,88"
        );

        CsvConfig config = CsvConfig.builder()
                .setHasHeader(true)
                .build();

        runRoundTripTest(data, config);
    }


    /**
     * Runs a round-trip test using default CSV configuration.
     *
     * @param data Raw CSV lines to test
     */
    private void runRoundTripTest(List<String> data) throws Exception {
        CsvConfig config = CsvConfig.builder().build();
        runRoundTripTest(data, config);
    }

    /**
     * Runs a round-trip test using strict line count.
     *
     * @param data Raw CSV lines to test
     */
    private void runRoundTripTest(List<String> data, CsvConfig config) throws Exception {
        int expected = config.hasHeader() ? data.size() - 1 : data.size();
        runRoundTripTest(data, config, expected);
    }


    /**
     * Runs a complete CSV round-trip:
     * input file -> CsvReader -> CsvWriter -> CsvReader -> compare results
     *
     * @param data   Raw CSV lines to test
     * @param config Custom CSV configuration
     */
    private void runRoundTripTest(List<String> data, CsvConfig config, int expectedRowCount) throws Exception {
        File input = File.createTempFile("input", ".csv");
        File output = File.createTempFile("output", ".csv");

        Files.write(input.toPath(), data);

        List<Row> originalRows = readRows(input, config);
        writeRows(output, originalRows, config);
        List<Row> finalRows = readRows(output, config);

        assertEquals(expectedRowCount, originalRows.size(),
                "Row count mismatch after first read");
        assertEquals(originalRows.size(), finalRows.size(),
                "Row count mismatch after round trip");

        for (int i = 0; i < originalRows.size(); i++) {
            assertEquals(
                    originalRows.get(i).getValues(),
                    finalRows.get(i).getValues(),
                    "Data mismatch at row " + i
            );
            assertEquals(
                    config.hasHeader() ? originalRows.get(i).getHeaders().getColumnNames() : null,
                    config.hasHeader() ? finalRows.get(i).getHeaders().getColumnNames() : null,
                    "Headers mismatch at row " + i
            );
        }
    }

    /**
     * Reads all rows from a CSV file using the provided configuration.
     *
     * @param file   CSV file
     * @param config CSV configuration
     * @return List of parsed rows
     */
    private List<Row> readRows(File file, CsvConfig config) throws Exception {
        List<Row> rows = new ArrayList<>();

        try (CsvReader reader = CsvReader.fromPath(file.toPath(), config)) {
            Row row;
            while ((row = reader.readRow()) != null) {
                rows.add(row);
            }
        }

        return rows;
    }

    /**
     * Writes all rows to a CSV file using the provided configuration.
     *
     * @param file   Output CSV file
     * @param rows   Rows to write
     * @param config CSV configuration
     */
    private void writeRows(File file, List<Row> rows, CsvConfig config) throws Exception {
        try (CsvWriter writer = CsvWriter.toPath(file.toPath(), config)) {
            for (Row row : rows) {
                writer.writeRow(row);
            }
        }
    }
}
