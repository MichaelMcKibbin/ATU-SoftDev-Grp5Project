package com.group5.csv.io;

import com.group5.csv.core.Headers;
import com.group5.csv.core.Row;
import com.group5.csv.exceptions.ParseException;
import com.group5.csv.testutils.VirtualReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;



class CsvReaderTest {

    /**
     * Tests the default configuration created by the {@code createConfig()} method of the {@link CsvReader} class.
     *
     * This test ensures that the {@link CsvConfig} object returned by the {@code createConfig()} method is initialized
     * with the correct default settings:
     * - The {@code format} is set to {@code CsvFormat.excel()}.
     * - The configuration expects a header row, verified by {@code hasHeader()}.
     * - The configuration does not require uniform field count, checked by {@code isRequireUniformFieldCount()}.
     * - Empty lines in the CSV are skipped, verified by {@code isSkipEmptyLines()}.
     * - The default charset is {@code StandardCharsets.UTF_8}.
     * - The Byte Order Mark (BOM) is written to the output, checked by {@code isWriteBOM()}.
     * - The size of the read buffer is set to 8192 bytes.
     *
     * This method verifies these default settings using assertions to ensure the consistency and reliability
     * of the CsvReader.reateConfig() implementation.
     */
    @Test
    void createConfig_usesExpectedDefaults() {
        CsvReader reader = new CsvReader(new VirtualReader(""));

        CsvConfig cfg = reader.getConfig();

        CsvFormat expectedFormat = CsvFormat.excel();
        String expectedFormatString = expectedFormat.toString();
        CsvFormat actualFormat = cfg.getFormat();
        String actualFormatString = actualFormat.toString();

        assertEquals(expectedFormatString, actualFormatString);
        assertTrue(cfg.hasHeader());
        assertFalse(cfg.isRequireUniformFieldCount());
        assertTrue(cfg.isSkipEmptyLines());
        assertEquals(StandardCharsets.UTF_8, cfg.getCharset());
        assertTrue(cfg.isWriteBOM());
        assertEquals(8192, cfg.getReadBufSize());

    }

    /**
     * Verifies that the {@link CsvConfig.Builder} correctly sets all fields as specified
     * during the builder configuration and that the resulting {@link CsvConfig} object
     * reflects these settings accurately.
     *
     * This test ensures the following:
     * - The selected CSV format is properly set.
     * - Header inclusion is accurately enabled or disabled.
     * - Uniform field count requirement is correctly configured.
     * - Empty line skipping behavior is properly set.
     * - The character set is configured as expected.
     * - BOM writing behavior is accurately set.
     * - The read buffer size is configured to the specified value.
     *
     * Assertions are made on the resulting {@link CsvConfig} instance to confirm each field
     * matches the values configured in the {@link CsvConfig.Builder}.
     */
    @Test
    void builderSetsAllFieldsCorrectly() {
        CsvConfig cfg = new CsvConfig.Builder()
                .setFormat(CsvFormat.excel())
                .setHasHeader(true)
                .setRequireUniformFieldCount(true)
                .setSkipEmptyLines(false)
                .setCharset(StandardCharsets.UTF_8)
                .setWriteBOM(false)
                .setReadBufSize(4096)
                .build();

        CsvFormat expectedFormat = CsvFormat.excel();
        String expectedFormatString = expectedFormat.toString();
        CsvFormat actualFormat = cfg.getFormat();
        String actualFormatString = actualFormat.toString();

        assertEquals(expectedFormatString, actualFormatString);
        assertTrue(cfg.hasHeader());
        assertTrue(cfg.isRequireUniformFieldCount());
        assertFalse(cfg.isSkipEmptyLines());
        assertEquals(StandardCharsets.UTF_8, cfg.getCharset());
        assertFalse(cfg.isWriteBOM());
        assertEquals(4096, cfg.getReadBufSize());

    }


    // ----- Helpers -----
    private CsvReader reader(String input, Consumer<CsvConfig.Builder> configurer) {
        return reader(input, configurer, null);
    }

    private CsvReader reader(String input, Consumer<CsvConfig.Builder> configurer, Headers headers)
    {
        CsvConfig.Builder b = new CsvConfig.Builder();
        configurer.accept(b);
        return new CsvReader(new VirtualReader(input), b.build(), headers);
    }

    private List<String> list(String... v) {
        return Arrays.asList(v);
    }

    private List<List<String>> rows(String[][] arr) {
        return Arrays.stream(arr)
                .map(Arrays::asList)
                .toList();
    }

    private void assertRowEquals(Row row,
                                 List<String> expectedValues,
                                 List<String> expectedHeaders)
    {
        assertEquals(expectedValues, row.getValues());
        assertEquals(expectedHeaders, row.getHeaders().getColumnNames());
    }

    protected void assertInsufficientWarning(CsvReader reader, long lineNumber) {
        CsvWarning w = reader.getLastRowWarning();
        assertNotNull(w, "Expected insufficient warning");
        assertEquals(CsvWarning.Type.TOO_FEW_FIELDS, w.type(), "Wrong warning type");
        assertEquals(lineNumber, w.line(), "Wrong line number in warning");
    }

    protected void assertExceedWarning(CsvReader reader, long lineNumber) {
        CsvWarning w = reader.getLastRowWarning();
        assertNotNull(w, "Expected exceed warning");
        assertEquals(CsvWarning.Type.TOO_MANY_FIELDS, w.type(), "Wrong warning type");
        assertEquals(lineNumber, w.line(), "Wrong line number in warning");
    }

    // ----- Constructors Tests -----
    @Test
    public void testConstructorStoresConfigReaderAndParser() throws Exception {
        CsvReader r = reader("a,b,c", b -> {
            b.setFormat(CsvFormat.excel());
            b.setHasHeader(false);
        });

        assertNotNull(r.getConfig());
        assertNotNull(r.getIn());
        assertNotNull(r.getParser());
        assertNull(r.getHeaders());
    }

    @Test
    public void testConstructorWithGivenHeaders() throws Exception {
        Headers headers = new Headers(List.of("X", "Y", "Z"));

        CsvReader r = reader("1,2,3\n4,5,6", b -> {
            b.setFormat(CsvFormat.excel());
            b.setHasHeader(false);
        }, headers);

        // header should be exactly the provided one
        assertSame(headers, r.getHeaders());

        Row row = r.readRow();
        assertEquals("1", row.get("X"));
        assertEquals("2", row.get("Y"));
        assertEquals("3", row.get("Z"));
    }

    @Test
    public void testGivenHeadersSkipAutoHeaderParsing() throws Exception {
        Headers headers = new Headers(List.of("A", "B"));

        CsvReader r = reader("NOT,HEADER\n1,2", b -> {
            b.setFormat(CsvFormat.excel());
            b.setHasHeader(true);  // normally would consume first line
        }, headers);

        // Because headers are provided, "NOT,HEADER" is treated as row data
        Row row1 = r.readRow();
        assertEquals("NOT", row1.get("A"));
        assertEquals("HEADER", row1.get("B"));

        Row row2 = r.readRow();
        assertEquals("1", row2.get("A"));
        assertEquals("2", row2.get("B"));
    }

    @Test
    public void testReaderConstructorWithConfigurer() throws Exception {
        CsvReader r = reader("x,y", b -> {
            b.setFormat(CsvFormat.excel());
            b.setHasHeader(false);
            b.setSkipEmptyLines(true);
        });

        assertFalse(r.getConfig().hasHeader());
        assertTrue(r.getConfig().isSkipEmptyLines());
    }

    @Test
    void testCloseClosesUnderlyingReader() throws Exception {
        VirtualReader vr = new VirtualReader("abc");

        CsvConfig cfg = new CsvConfig.Builder()
                .setFormat(CsvFormat.excel())
                .setHasHeader(false)
                .build();

        CsvReader reader = new CsvReader(vr, cfg);

        // Before close, reading must work
        assertDoesNotThrow(reader::readRow);

        // Close the CsvReader → must close underlying VirtualReader
        reader.close();

        // Now VirtualReader should throw when attempting to read
        assertThrows(IOException.class, vr::read);
    }

    @Test
    void constructorWithInputStreamWorks() throws IOException {
        // Arrange: VirtualReader wrapped as InputStream
        String csvData = "a,b,c\n1,2,3";
        ByteArrayInputStream input = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        CsvReader reader = new CsvReader(input);

        // Act: read rows
        List<Row> rows = assertDoesNotThrow(reader::readAll);

        // Assert: rows are read correctly
        assertEquals(1, rows.size()); // only the second line is returned as row
        assertEquals(List.of("1", "2", "3"), rows.get(0).getValues());

        // Assert: headers are recognized correctly
        assertEquals(List.of("a", "b", "c"), reader.getHeaders().getColumnNames());

        // Cleanup
        reader.close();
    }


    // ----- Behaviour Tests -----
    @Test
    void readRowThrowsIOExceptionWhenUnderlyingReaderClosed() throws IOException {
        // Prepare VirtualReader and CsvReader
        VirtualReader vr = new VirtualReader("a,b,c\nd,e");
        CsvReader reader = new CsvReader(vr, new CsvConfig.Builder().setHasHeader(false).build());

        // Close the underlying reader to simulate IOException
        vr.close();

        // readRow should now throw IOException
        assertThrows(IOException.class, reader::readRow);
    }

    @Test
    void readsDocumentPerRowCorrectly() throws IOException {
        CsvReader reader = reader("a,b,c\nd,e\nf",
                cfg -> cfg.setHasHeader(false)
                        .setRequireUniformFieldCount(false));

        List<List<String>> expectedData = rows(new String[][]{
                {"a", "b", "c"},
                {"d", "e"},
                {"f"}
        });

        List<List<String>> expectedHeaders = rows(new String[][]{
                {"col0", "col1", "col2"},
                {"col0", "col1"},
                {"col0"}
        });

        for (int i = 0; i < expectedData.size(); ++i) {
            Row row = assertDoesNotThrow(reader::readRow);
            assertRowEquals(row, expectedData.get(i), expectedHeaders.get(i));
        }

        assertNull(reader.readRow()); // input exhausted
    }

    @Test
    void readsDocumentAtOnceCorrectly() throws IOException {
        CsvReader reader = reader("a,b,c\nd,e\nf",
                cfg -> cfg.setHasHeader(false)
                        .setRequireUniformFieldCount(false));

        List<List<String>> expectedData = rows(new String[][]{
                {"a", "b", "c"},
                {"d", "e"},
                {"f"}
        });

        List<List<String>> expectedHeaders = rows(new String[][]{
                {"col0", "col1", "col2"},
                {"col0", "col1"},
                {"col0"}
        });

        List<Row> rows = assertDoesNotThrow(reader::readAll);
        assertEquals(3, rows.size());

        for (int i = 0; i < rows.size(); ++i) {
            assertRowEquals(rows.get(i), expectedData.get(i), expectedHeaders.get(i));
        }

        assertNull(reader.readRow()); // input exhausted
    }

    @Test
    void readsDocumentSkippingEmptyLines() throws IOException {
        CsvReader reader = reader("a,b,c\n\n\nd,e\n\nf",
                cfg -> cfg.setHasHeader(false)
                        .setRequireUniformFieldCount(false)
                        .setSkipEmptyLines(true));

        List<List<String>> expectedData = rows(new String[][]{
                {"a", "b", "c"},
                {"d", "e"},
                {"f"}
        });

        List<List<String>> expectedHeaders = rows(new String[][]{
                {"col0", "col1", "col2"},
                {"col0", "col1"},
                {"col0"}
        });

        List<Row> rows = assertDoesNotThrow(reader::readAll);
        assertEquals(3, rows.size());

        for (int i = 0; i < rows.size(); ++i) {
            assertRowEquals(rows.get(i), expectedData.get(i), expectedHeaders.get(i));
        }
    }

    @Test
    void readsDocumentWithEmptyLines() throws IOException {
        CsvReader reader = reader("a,b,c\n\n\nd,e\n\nf",
                cfg -> cfg.setHasHeader(false)
                        .setRequireUniformFieldCount(false)
                        .setSkipEmptyLines(false));

        List<List<String>> expectedData = rows(new String[][]{
                {"a", "b", "c"},
                {},                 // empty line
                {},                 // another empty line
                {"d", "e"},
                {},                 // empty line
                {"f"}
        });

        List<List<String>> expectedHeaders = rows(new String[][]{
                {"col0", "col1", "col2"},
                {},                 // empty line → no headers
                {},                 // empty line → no headers
                {"col0", "col1"},
                {},                 // empty line
                {"col0"}            // single column
        });

        List<Row> rows = assertDoesNotThrow(reader::readAll);
        assertEquals(6, rows.size());

        for (int i = 0; i < rows.size(); ++i) {
            assertRowEquals(rows.get(i), expectedData.get(i), expectedHeaders.get(i));
        }
    }

    @Test
    void readsDocumentWithUFCIncorrectFieldCount() throws IOException {
        CsvReader reader = reader("a,b,c\nd,e\n\nf,g,h,i\nj",
                cfg -> cfg.setRequireUniformFieldCount(true)
                        .setSkipEmptyLines(false)
                        .setHasHeader(false));

        List<List<String>> expectedData = rows(new String[][]{
                {"a", "b", "c"},
                {"d", "e", ""},
                {"", "", ""},
                {"f", "g", "h"},
                {"j", "", ""}
        });

        List<List<String>> expectedHeaders = rows(new String[][]{
                {"col0", "col1", "col2"},
                {"col0", "col1", "col2"},
                {"col0", "col1", "col2"},
                {"col0", "col1", "col2"},
                {"col0", "col1", "col2"}
        });

        for (int i = 0; i < 5; ++i) {
            Row row = assertDoesNotThrow(reader::readRow);
            assertNotNull(row);

            assertRowEquals(row, expectedData.get(i), expectedHeaders.get(i));

            // --- Warning checks ---
            if (i == 1 || i == 2 || i == 4) {   // insufficient fields
                assertInsufficientWarning(reader, i + 1);
            }
            else if (i == 3) {                 // too many fields
                assertExceedWarning(reader, i + 1);
            }
        }

        // No more rows
        assertNull(reader.readRow());
    }

    @Test
    void readsDocumentWithSuppliedHeaders() throws IOException {
        List<String> headerNames = List.of("header0", "header1", "header2");
        Headers suppliedHeaders = new Headers(headerNames);

        CsvReader reader = reader(
                "a,b,c\nd,e\n\nf,g,h,i\nj",
                cfg -> cfg.setRequireUniformFieldCount(true)
                        .setSkipEmptyLines(false)
                        .setHasHeader(true),
                suppliedHeaders
        );

        List<List<String>> expectedData = rows(new String[][]{
                {"a", "b", "c"},
                {"d", "e", ""},
                {"", "", ""},
                {"f", "g", "h"},
                {"j", "", ""}
        });

        List<List<String>> expectedHeaders = rows(new String[][]{
                {"header0", "header1", "header2"},
                {"header0", "header1", "header2"},
                {"header0", "header1", "header2"},
                {"header0", "header1", "header2"},
                {"header0", "header1", "header2"}
        });

        for (int i = 0; i < 5; ++i) {
            Row row = assertDoesNotThrow(reader::readRow);
            assertNotNull(row);

            assertRowEquals(row, expectedData.get(i), expectedHeaders.get(i));

            // --- Warning checks ---
            if (i == 1 || i == 2 || i == 4) {          // insufficient fields
                assertInsufficientWarning(reader, i + 1);
            }
            else if (i == 3) {                         // too many fields
                assertExceedWarning(reader, i + 1);
            }
        }

        // No more rows
        assertNull(reader.readRow());
    }

    @Test
    void readsDocumentWithRecognisedHeaders() throws IOException {
        CsvReader reader = reader(
                "a,b,c\nd,e\n\nf,g,h,i\nj",
                cfg -> cfg.setRequireUniformFieldCount(true)
                        .setSkipEmptyLines(false)
                        .setHasHeader(true)
        );

        List<List<String>> expectedData = rows(new String[][]{
                {"d", "e", ""},   // row 1 after skipping header
                {"", "", ""},     // empty line → padded
                {"f", "g", "h"},  // extra field trimmed
                {"j", "", ""}     // last line padded
        });

        List<List<String>> expectedHeaders = rows(new String[][]{
                {"a", "b", "c"},
                {"a", "b", "c"},
                {"a", "b", "c"},
                {"a", "b", "c"}
        });

        for (int i = 0; i < 4; ++i) {
            Row row = assertDoesNotThrow(reader::readRow);
            assertNotNull(row);

            assertRowEquals(row, expectedData.get(i), expectedHeaders.get(i));

            // --- Warning checks ---
            // original logic: insufficient on rows 0,1,3
            if (i == 0 || i == 1 || i == 3) {
                assertInsufficientWarning(reader, i + 2);
            }
            // row 2: "exceed"
            else if (i == 2) {
                assertExceedWarning(reader, i + 2);
            }
        }

        assertNull(reader.readRow());
    }

    @Test
    void readRowThrowsParseExceptionWithLineNumberSkipEmptyLinesTrue() {
        // Arrange: VirtualReader that always throws ParseException
        CsvReader r = new CsvReader(new VirtualReader("bad,line") {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new ParseException("parse error");
            }
        }, new CsvConfig.Builder()
                .setHasHeader(false)
                .setSkipEmptyLines(true)
                .build());

        // Act & Assert
        ParseException ex = assertThrows(ParseException.class, r::readRow);
        assertEquals(0, ex.getLine()); // first line = 0 before increment
        assertTrue(ex.getMessage().toLowerCase().contains("parse"));
    }

    @Test
    void readRowThrowsParseExceptionWithLineNumberSkipEmptyLinesFalse() {
        // Arrange: VirtualReader that always throws ParseException
        CsvReader r = new CsvReader(new VirtualReader("bad,line") {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new ParseException("parse error");
            }
        }, new CsvConfig.Builder()
                .setHasHeader(false)
                .setSkipEmptyLines(false)
                .build());

        // Act & Assert
        ParseException ex = assertThrows(ParseException.class, r::readRow);
        assertEquals(0, ex.getLine()); // first line = 0 before increment
        assertTrue(ex.getMessage().toLowerCase().contains("parse"));
    }


    // ----- Iterator Tests -----
    @Nested
    class CsvReaderIteratorTest {
        @Test
        void iteratorReturnsAllRowsCorrectly() throws Exception {
            CsvReader r = reader("a,b,c\nd,e\nf", b -> b.setHasHeader(false));

            List<String> expectedFirstValues = List.of("a", "b", "c");
            List<String> expectedSecondValues = List.of("d", "e");
            List<String> expectedThirdValues = List.of("f");

            List<Row> rows = new ArrayList<>();
            for (Row row : r) {
                rows.add(row);
            }

            assertEquals(3, rows.size());
            assertEquals(expectedFirstValues, rows.get(0).getValues());
            assertEquals(expectedSecondValues, rows.get(1).getValues());
            assertEquals(expectedThirdValues, rows.get(2).getValues());
        }

        @Test
        void hasNextAndNextWorkCorrectly() throws Exception {
            CsvReader r = reader("x,y\n1,2", b -> b.setHasHeader(false));

            var it = r.iterator();

            assertTrue(it.hasNext());
            Row first = it.next();
            assertEquals(List.of("x", "y"), first.getValues());

            assertTrue(it.hasNext());
            Row second = it.next();
            assertEquals(List.of("1", "2"), second.getValues());

            assertFalse(it.hasNext());
            assertThrows(NoSuchElementException.class, it::next);
        }

        @Test
        void iteratorWithEmptyCsvReturnsNoRows() throws Exception {
            CsvReader r = reader("", b -> b.setHasHeader(false));
            var it = r.iterator();
            assertFalse(it.hasNext());
            assertThrows(NoSuchElementException.class, it::next);
        }

        @Test
        void iteratorWorksWithSuppliedHeaders() throws Exception {
            Headers headers = new Headers(List.of("A", "B"));
            CsvReader r = reader("1,2\n3,4", b -> b.setHasHeader(false), headers);

            List<Row> rows = new ArrayList<>();
            for (Row row : r) {
                rows.add(row);
            }

            assertEquals("1", rows.get(0).get("A"));
            assertEquals("2", rows.get(0).get("B"));
            assertEquals("3", rows.get(1).get("A"));
            assertEquals("4", rows.get(1).get("B"));
        }

        @Test
        void iteratorStopsAtEof() throws Exception {
            CsvReader r = reader("val", b -> b.setHasHeader(false));
            var it = r.iterator();
            Row row = it.next();
            assertEquals(List.of("val"), row.getValues());

            assertFalse(it.hasNext());
            assertThrows(NoSuchElementException.class, it::next);
        }

        @Test
        void iteratorNextThrowsUncheckedIOExceptionWhenUnderlyingReaderClosed() {
            // Arrange: VirtualReader and CsvReader
            VirtualReader vr = new VirtualReader("a,b,c\nd,e");
            CsvConfig config = new CsvConfig.Builder().setHasHeader(false).build();
            CsvReader r = new CsvReader(vr, config);

            // Close the underlying VirtualReader to simulate IOException
            vr.close();

            // Act & Assert
            var it = r.iterator();
            UncheckedIOException ex = assertThrows(UncheckedIOException.class, it::next);
            assertTrue(ex.getCause() instanceof IOException);
        }
    }

}
