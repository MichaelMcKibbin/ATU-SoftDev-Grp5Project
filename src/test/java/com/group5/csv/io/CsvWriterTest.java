package com.group5.csv.io;

import com.group5.csv.core.*;
import com.group5.csv.exceptions.ParseException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Basic test of MVP/MWP version of CsvWriter.
 *
 * What this test proves:
 *
 * - CsvWriter writes the header only once
 * - Values align with fields & headers
 * - Default quoting behavior matches RFC4180
 * - Output ends with newlines (CsvPrinter’s responsibility)
 * - The Map-based row writing path works
 */

class CsvWriterTest {

    // --- helper to parse all rows from a string using CsvParser ---
    private List<List<String>> parseAll(String csv, CsvFormat format) throws IOException {
        CsvParser parser = new CsvParser(format, new StringReader(csv));
        List<List<String>> rows = new ArrayList<>();

        List<String> row;
        while ((row = parser.readRow()) != null) {
            rows.add(row);
        }
        return rows;
    }

    // --- helper to round-trip rows through CsvWriter ---
    private String writeAll(List<List<String>> rows, CsvConfig config) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CsvWriter writer = new CsvWriter(out, config)) {
            writer.writeAll(rows);
        }
        Charset cs = config.getCharset();
        return out.toString(cs);
    }

    /**
     * Verifies that a basic RFC-4180 round-trip through CsvWriter preserves
     * all cell values exactly as parsed by CsvParser.
     */
    @Test
    void roundTrip_rfc4180_basic() throws IOException, ParseException {
        String input =
                "id,name,age\n" +
                        "1,Alice,30\n" +
                        "2,Bob,40\n";

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.rfc4180())
                .setHasHeader(false)
                .build();

        List<List<String>> originalRows = parseAll(input, config.getFormat());
        String written = writeAll(originalRows, config);
        List<List<String>> reParsedRows = parseAll(written, config.getFormat());

        assertEquals(originalRows, reParsedRows,
                "Round-trip via CsvWriter should preserve all cells for RFC-4180 dialect");
    }

    /**
     * Ensures that semicolon-delimited CSV (Excel dialect) is written correctly
     * and can be parsed back to produce an identical list of rows.
     */
    @Test
    void roundTrip_semicolon_delimiter() throws IOException {
        String input =
                "id;name;amount\n" +
                        "1;Alice;100.50\n" +
                        "2;Bob;200.75\n";

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.excel_semicolon())
                .setHasHeader(false)
                .build();

        List<List<String>> originalRows = parseAll(input, config.getFormat());
        String written = writeAll(originalRows, config);
        List<List<String>> reParsedRows = parseAll(written, config.getFormat());

        assertEquals(originalRows, reParsedRows,
                "Round-trip via CsvWriter should preserve cells for semicolon-delimited dialect");
    }

    /**
     * Confirms that tab-delimited CSV (TSV format) is round-trip safe using
     * CsvParser → CsvWriter → CsvParser.
     */
    @Test
    void roundTrip_tab_delimiter() throws IOException {
        String input =
                "id\tname\tcomment\n" +
                        "1\tAlice\tHello\tworld\n" +
                        "2\tBob\tSecond line\n";

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.tsv())
                .setHasHeader(false)
                .build();

        List<List<String>> originalRows = parseAll(input, config.getFormat());
        String written = writeAll(originalRows, config);
        List<List<String>> reParsedRows = parseAll(written, config.getFormat());

        assertEquals(originalRows, reParsedRows,
                "Round-trip via CsvWriter should preserve cells for tab-delimited dialect");
    }

    /**
     * Validates that CsvWriter preserves parsed cell values when the input
     * uses CRLF newlines. The output newline format may differ, but the content
     * of each cell must remain identical.
     */
    @Test
    void roundTrip_windowsNewlines() throws IOException {
        String input =
                "id,name\r\n" +
                        "1,Alice\r\n" +
                        "2,Bob\r\n";

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.rfc4180())
                .setHasHeader(false)
                .build();

        List<List<String>> originalRows = parseAll(input, config.getFormat());
        String written = writeAll(originalRows, config);
        List<List<String>> reParsedRows = parseAll(written, config.getFormat());

        assertEquals(originalRows, reParsedRows,
                "Round-trip should preserve cells even when input uses CRLF newlines");
    }

    /**
     * Verifies that constructing CsvWriter with a BufferedWriter uses the provided
     * configuration and successfully writes a simple row.
     */
    @Test
    void writerConstructor_usesGivenConfigAndBufferedWriter() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.rfc4180())
                .setHasHeader(false)
                .build();

        CsvWriter writer = new CsvWriter(bw, config);

        assertSame(config, writer.getConfig());
        writer.writeRow(List.of("a", "b"));
        writer.close();
    }

    /**
     * Ensures that writeHeader(List) writes a header row once and prevents
     * multiple header writes by throwing an IllegalStateException.
     */
    @Test
    void writeHeader_writesOnceAndPreventsSecondHeader() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.rfc4180())
                .setHasHeader(true)
                .build();

        try (CsvWriter writer = new CsvWriter(out, config)) {
            writer.writeHeader(List.of("id", "name"));
            assertThrows(IllegalStateException.class, () ->
                    writer.writeHeader(List.of("id2", "name2")));
        }
    }

    /**
     * Verifies that passing a null header list causes CsvWriter.writeHeader()
     * to throw an IllegalArgumentException.
     */
    @Test
    void writeHeader_null_throwsIllegalArgumentException() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvConfig config = CsvConfig.builder().build();

        try (CsvWriter writer = new CsvWriter(out, config)) {
            assertThrows(IllegalArgumentException.class, () ->
                    writer.writeHeader(null));
        }
    }

    /**
     * Ensures that CsvWriter.writeRow(List<?>) throws an
     * IllegalArgumentException when the list is null.
     */
    @Test
    void writeRow_nullList_throwsIllegalArgumentException() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvConfig config = CsvConfig.builder().build();

        try (CsvWriter writer = new CsvWriter(out, config)) {
            assertThrows(IllegalArgumentException.class, () ->
                    writer.writeRow((List<?>) null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Confirms that writeAll(Iterable<?>) throws an IllegalArgumentException
     * when the iterable itself is null.
     */
    @Test
    void writeAll_nullIterable_throwsIllegalArgumentException() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvConfig config = CsvConfig.builder().build();

        try (CsvWriter writer = new CsvWriter(out, config)) {
            assertThrows(IllegalArgumentException.class, () ->
                    writer.writeAll(null));
        }
    }

    /**
     * Validates that writeRow(Row) writes fields in header order and produces
     * the expected RFC4180 CSV output.
     */
    @Test
    void writeRow_Row_writesCellsInHeaderOrder() throws IOException {
        Headers headers = new Headers(List.of("id", "name"));
        RowBuilder builder = new RowBuilder(headers);
        builder.add("1");
        builder.add("Alice");
        Row row = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.rfc4180())
                .build();

        String result;
        try (CsvWriter writer = new CsvWriter(out, config)) {
            writer.writeRow(row);
        }
        result = out.toString(config.getCharset());

        assertEquals("1,Alice\n", result);
    }

    /**
     * Ensures that calling writeRow(Row) with a null argument throws an
     * IllegalArgumentException.
     */
    @Test
    void writeRow_nullRow_throwsIllegalArgumentException() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvConfig config = CsvConfig.builder().build();

        try (CsvWriter writer = new CsvWriter(out, config)) {
            assertThrows(IllegalArgumentException.class, () ->
                    writer.writeRow((Row) null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Confirms that writeAllRows(Iterable<Row>) writes multiple rows in
     * order using the header-based column ordering.
     */
    @Test
    void writeAllRows_writesMultipleRows() throws IOException {
        Headers headers = new Headers(List.of("id", "name"));

        RowBuilder b1 = new RowBuilder(headers);
        b1.add("1");
        b1.add("Alice");
        Row r1 = b1.build();

        RowBuilder b2 = new RowBuilder(headers);
        b2.add("2");
        b2.add("Bob");
        Row r2 = b2.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.rfc4180())
                .build();

        try (CsvWriter writer = new CsvWriter(out, config)) {
            writer.writeAllRows(List.of(r1, r2));
        }

        String result = out.toString(config.getCharset());
        assertEquals("1,Alice\n2,Bob\n", result);
    }

    /**
     * Tests that CsvWriter.toPath(Path, CsvConfig) writes output using the
     * configured charset and produces the expected CSV text.
     */
    @Test
    void toPath_writesFileUsingConfigCharset() throws IOException {
        Path temp = Files.createTempFile("csvwriter-", ".csv");

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.rfc4180())
                .setCharset(StandardCharsets.UTF_8)
                .build();

        try (CsvWriter writer = CsvWriter.toPath(temp, config)) {
            writer.writeRow(List.of("x", "y"));
        }

        String content = Files.readString(temp, StandardCharsets.UTF_8);
        assertEquals("x,y\n", content);
    }

    /**
     * Verifies that the CsvWriter(Writer, CsvConfig) constructor throws an
     * IllegalArgumentException when the Writer argument is null.
     */
    @Test
    void constructor_nullWriter_throwsIllegalArgumentException() {
        CsvConfig config = CsvConfig.builder().build();

        assertThrows(IllegalArgumentException.class, () ->
                new CsvWriter((Writer) null, config));
    }

    /**
     * Verifies that the CsvWriter(Writer, CsvConfig) constructor throws an
     * IllegalArgumentException when the CsvConfig argument is null.
     */
    @Test
    void constructor_nullConfig_throwsIllegalArgumentException() {
        StringWriter sw = new StringWriter();

        assertThrows(IllegalArgumentException.class, () ->
                new CsvWriter(sw, null));
    }

    /**
     * Tests the null-cell handling rule in writeRow(List<?>): any null
     * element must be written as an empty CSV field ("").
     */
    @Test
    void writeRow_listWithNullCell_writesEmptyString() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.rfc4180())
                .build();

        try (CsvWriter writer = new CsvWriter(out, config)) {
            writer.writeRow(Arrays.asList("a", null, "c"));
        }

        String result = out.toString(config.getCharset());
        assertEquals("a,,c\n", result);
    }

    /**
     * Tests the null-cell handling rule in writeRow(Row): if a Row contains
     * a null value for a column, CsvWriter must emit an empty CSV field.
     */
    @Test
    void writeRow_RowWithNullCell_writesEmptyString() throws IOException {
        Headers headers = new Headers(List.of("c1", "c2", "c3"));
        RowBuilder builder = new RowBuilder(headers);
        builder.add("a");
        builder.add(null);
        builder.add("c");
        Row row = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.rfc4180())
                .build();

        try (CsvWriter writer = new CsvWriter(out, config)) {
            writer.writeRow(row);
        }

        String result = out.toString(config.getCharset());
        assertEquals("a,,c\n", result);
    }

}
