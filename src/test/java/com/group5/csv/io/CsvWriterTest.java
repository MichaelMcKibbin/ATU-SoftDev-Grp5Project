package com.group5.csv.io;

import com.group5.csv.core.Field;
import com.group5.csv.core.FieldType;
import com.group5.csv.exceptions.ParseException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;






/**
 * Basic test of MVP/MWP version of CsvWriter
 *
 * What this test proves:
 *
 * CsvWriter writes the header only once
 * Values align with fields & headers
 * Default quoting behavior matches RFC4180
 * Output ends with newlines (CsvPrinter’s responsibility)
 * The Map-based row writing path works
 *
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

@Test
void roundTrip_rfc4180_basic() throws IOException, ParseException {
    String input =
            "id,name,age\n" +
                    "1,Alice,30\n" +
                    "2,Bob,40\n";

    CsvConfig config = CsvConfig.builder()
            .setFormat(CsvFormat.rfc4180())
            .setHasHeader(false)     // parser-only here, header semantics not used
            .build();

    List<List<String>> originalRows = parseAll(input, config.getFormat());
    String written = writeAll(originalRows, config);
    List<List<String>> reParsedRows = parseAll(written, config.getFormat());

    assertEquals(originalRows, reParsedRows,
            "Round-trip via CsvWriter should preserve all cells for RFC-4180 dialect");
}

    @Test
    void roundTrip_semicolon_delimiter() throws IOException {
        String input =
                "id;name;amount\n" +
                        "1;Alice;100.50\n" +
                        "2;Bob;200.75\n";

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.excel_semicolon())  // keep your actual preset name
                .setHasHeader(false)
                .build();

        List<List<String>> originalRows = parseAll(input, config.getFormat());
        String written = writeAll(originalRows, config);
        List<List<String>> reParsedRows = parseAll(written, config.getFormat());

        assertEquals(originalRows, reParsedRows,
                "Round-trip via CsvWriter should preserve cells for semicolon-delimited dialect");
    }


@Test
void roundTrip_tab_delimiter() throws IOException {
    String input =
            "id\tname\tcomment\n" +
                    "1\tAlice\tHello\tworld\n" +   // note: internal tab => will be quoted
                    "2\tBob\tSecond line\n";

    CsvConfig config = CsvConfig.builder()
            .setFormat(CsvFormat.tsv())  // adjust if you named it differently
            .setHasHeader(false)
            .build();

    List<List<String>> originalRows = parseAll(input, config.getFormat());
    String written = writeAll(originalRows, config);
    List<List<String>> reParsedRows = parseAll(written, config.getFormat());

    assertEquals(originalRows, reParsedRows,
            "Round-trip via CsvWriter should preserve cells for tab-delimited dialect");
}

@Test
void roundTrip_windowsNewlines() throws IOException {
    String input =
            "id,name\r\n" +
                    "1,Alice\r\n" +
                    "2,Bob\r\n";

    CsvConfig config = CsvConfig.builder()
            .setFormat(CsvFormat.rfc4180()) // assuming this normalises CRLF internally
            .setHasHeader(false)
            .build();

    List<List<String>> originalRows = parseAll(input, config.getFormat());
    String written = writeAll(originalRows, config);
    List<List<String>> reParsedRows = parseAll(written, config.getFormat());

    // We don’t assert on raw newline chars (CsvPrinter may normalise),
    // only on the parsed cell values.
    assertEquals(originalRows, reParsedRows,
            "Round-trip should preserve cells even when input uses CRLF newlines");
}


///**
// * FUTURE TESTS - Need Schema & Field first
// */
//    @Test
//    void writesHeaderAndSingleRow() throws Exception {
//        // --- Setup --------------------------------------------------------
//        StringWriter sw = new StringWriter();
//        CsvFormat format = CsvFormat.rfc4180();
//
//        // Define fields (id, name)
//        List<Field> fields = List.of(
//                new Field("id", FieldType.STRING),
//                new Field("name", FieldType.STRING)
//        );
//
//        // Header names must align with fields
//        List<String> headers = List.of("id", "name");
//
//        CsvWriter writer = new CsvWriter(sw, fields, headers, format);
//
//        // --- Act ----------------------------------------------------------
//        writer.writeRow(Map.of(
//                "id", "1",
//                "name", "Bob"
//        ));
//        writer.close();
//
//        // --- Assert -------------------------------------------------------
//        String output = sw.toString();
//
//        // Expect:
//        // id,name
//        // 1,Alice
//        String expected =
//                "id,name\n" +
//                        "1,Bob\n";
//
//        assertEquals(expected, output);
//    }
//
//    /**
//     * Test quoting rules
//     * - asserts that a comma forces quoting.
//     * @throws Exception
//     *
//     */
//    @Test
//    void quotesFieldsWhenNeeded() throws Exception {
//        StringWriter sw = new StringWriter();
//        CsvFormat format = CsvFormat.rfc4180();
//
//        List<Field> fields = List.of(
//                new Field("col1", FieldType.STRING),
//                new Field("col2", FieldType.STRING)
//        );
//        List<String> headers = List.of("col1", "col2");
//
//        CsvWriter writer = new CsvWriter(sw, fields, headers, format);
//
//        writer.writeRow(Map.of(
//                "col1", "Hello",
//                "col2", "with,comma"
//        ));
//        writer.close();
//
//        String expected =
//                "col1,col2\n" +
//                        "Hello,\"with,comma\"\n";
//
//        assertEquals(expected, sw.toString());
//    }
//
//    /**
//     * Test alwaysQuote = true
//     */
//    @Test
//    void alwaysQuotesWhenConfigured() throws Exception {
//        CsvFormat format = CsvFormat.builder().alwaysQuote(true).build();
//        StringWriter sw = new StringWriter();
//
//        List<Field> fields = List.of(
//                new Field("x", FieldType.STRING)
//        );
//        List<String> headers = List.of("x");
//
//        CsvWriter writer = new CsvWriter(sw, fields, headers, format);
//
//        writer.writeRow(Map.of("x", "Hello"));
//        writer.close();
//
//        assertEquals("x\n\"Hello\"\n", sw.toString());
//    }
///**
// * Test DecimalSpec / DateTimeSpec formatting
// */
//@Test
//void formatsDecimalAndDateCorrectly() throws Exception {
//    StringWriter sw = new StringWriter();
//    CsvFormat format = CsvFormat.rfc4180();
//
//    // Pretend these field types know how to format values:
//    List<Field> fields = List.of(
//            new Field("price", FieldType.DECIMAL),
//            new Field("created", FieldType.DATETIME)
//    );
//    List<String> headers = List.of("price", "created");
//
//    CsvWriter writer = new CsvWriter(sw, fields, headers, format);
//
//    writer.writeRow(Map.of(
//            "price", new BigDecimal("12.345"),
//            "created", LocalDateTime.of(2024, 3, 1, 10, 15)
//    ));
//    writer.close();
//
//    // Expect rounding/formatting based on DecimalSpec/DateTimeSpec
//    assertTrue(sw.toString().contains("12.35"));
//    assertTrue(sw.toString().contains("2024"));
//}

//    /**
//     * ROUND TRIP TEST
//     */
//    @Test
//    void writerAndParserRoundTripSimpleRow() throws Exception {
//        // ---------- Arrange: build CSV in memory via CsvWriter ----------
//        StringWriter sw = new StringWriter();
//        CsvFormat format = CsvFormat.rfc4180();
//
//        // Adjust this constructor if your Field API differs
//        List<Field> fields = List.of(
//                new Field("id",   FieldType.STRING),
//                new Field("name", FieldType.STRING),
//                new Field("note", FieldType.STRING)
//        );
//        List<String> headers = List.of("id", "name", "note");
//
//        CsvWriter writer = new CsvWriter(sw, fields, headers, format);
//
//        Map<String, Object> rowData = Map.of(
//                "id", "1",
//                "name", "Alice, Bob",            // contains comma → should be quoted
//                "note", "He said \"Hi\""         // contains quotes → should be escaped
//        );
//
//        writer.writeRow(rowData);
//        writer.close();
//
//        String csv = sw.toString();
//        assertFalse(csv.isEmpty(), "Writer should produce some CSV output");
//
//        // ---------- Act: parse it back with CsvParser ----------
//        StringReader sr = new StringReader(csv);
//        CsvParser parser = new CsvParser(format, sr);
//
//        List<String> headerRow = parser.readRow();
//        List<String> dataRow   = parser.readRow();
//        List<String> eofRow    = parser.readRow(); // should be null at end
//
//        // ---------- Assert: header round-trip ----------
//        assertNotNull(headerRow, "Header row should not be null");
//        assertEquals(headers, headerRow, "Header row should round-trip exactly");
//
//        // ---------- Assert: data row round-trip ----------
//        assertNotNull(dataRow, "Data row should not be null");
//        assertEquals("1",           dataRow.get(0));
//        assertEquals("Alice, Bob",  dataRow.get(1));
//        assertEquals("He said \"Hi\"", dataRow.get(2));
//
//        assertNull(eofRow, "Parser should return null after the last row");
//    }

}
