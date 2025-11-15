package com.group5.csv.io;

import com.group5.csv.core.Field;
import com.group5.csv.core.FieldType;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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

}
