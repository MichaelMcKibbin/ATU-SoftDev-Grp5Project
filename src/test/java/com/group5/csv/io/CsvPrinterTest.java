package com.group5.csv.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CsvPrinterTest {

    @Test
    void writesSimpleRowWithoutQuoting() throws IOException {
        StringWriter out = new StringWriter();
        CsvFormat fmt = CsvFormat.rfc4180();  // comma, ",", "\n"

        CsvPrinter printer = new CsvPrinter(out, fmt);
        printer.printRow(List.of("a", "b", "c"));
        printer.close();

        assertEquals("a,b,c\n", out.toString());
    }

    @Test
    void quotesFieldContainingDelimiter() throws IOException {
        StringWriter out = new StringWriter();
        CsvFormat fmt = CsvFormat.rfc4180();

        CsvPrinter printer = new CsvPrinter(out, fmt);
        printer.printRow(List.of("a", "b,c", "d"));
        printer.close();

        assertEquals("a,\"b,c\",d\n", out.toString());
    }

    @Test
    void quotesFieldContainingNewline() throws IOException {
        StringWriter out = new StringWriter();
        CsvFormat fmt = CsvFormat.rfc4180();

        CsvPrinter printer = new CsvPrinter(out, fmt);
        printer.printRow(List.of("line1\nline2"));
        printer.close();

        assertEquals("\"line1\nline2\"\n", out.toString());
    }

    @Test
    void escapesQuotesInsideQuotedField() throws IOException {
        StringWriter out = new StringWriter();
        CsvFormat fmt = CsvFormat.rfc4180();

        CsvPrinter printer = new CsvPrinter(out, fmt);
        printer.printRow(List.of("He said \"Hi\""));
        printer.close();

        // He said "Hi"  ->  "He said ""Hi"""
        assertEquals("\"He said \"\"Hi\"\"\"\n", out.toString());
    }

    @Test
    void quotesFieldsWithLeadingOrTrailingSpaces() throws IOException {
        StringWriter out = new StringWriter();
        CsvFormat fmt = CsvFormat.rfc4180();

        CsvPrinter printer = new CsvPrinter(out, fmt);
        printer.printRow(List.of("  leading", "trailing  ", " both "));
        printer.close();

        assertEquals("\"  leading\",\"trailing  \",\" both \"\n", out.toString());
    }

    @Test
    void treatsNullAsEmptyString() throws IOException {
        StringWriter out = new StringWriter();
        CsvFormat fmt = CsvFormat.rfc4180();

        CsvPrinter printer = new CsvPrinter(out, fmt);
        // Arrays.asList *does* allow null elements
        printer.printRow(java.util.Arrays.asList("a", null, "c"));
        printer.close();

        // null becomes empty string between delimiters
        assertEquals("a,,c\n", out.toString());
    }


    @Test
    void alwaysQuoteQuotesAllFields() throws IOException {
        StringWriter out = new StringWriter();
        CsvFormat fmt = CsvFormat.builder()
                .alwaysQuote(true)
                .build();

        CsvPrinter printer = new CsvPrinter(out, fmt);
        printer.printRow(List.of("a", "b", "c"));
        printer.close();

        assertEquals("\"a\",\"b\",\"c\"\n", out.toString());
    }

    @Test
    void usesConfiguredNewlineFromFormat() throws IOException {
        StringWriter out = new StringWriter();
        CsvFormat fmt = CsvFormat.builder()
                .newline("\r\n")   // Windows-style
                .build();

        CsvPrinter printer = new CsvPrinter(out, fmt);
        printer.printRow(List.of("x", "y"));
        printer.close();

        assertEquals("x,y\r\n", out.toString());
    }
}
