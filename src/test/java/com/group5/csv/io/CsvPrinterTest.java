package com.group5.csv.io;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvPrinter}.
 *
 * The tests are grouped using {@link Nested} classes to make it easier
 * to see which aspects of behaviour are being exercised:
 * <ul>
 *     <li>Core behaviour (simple rows, empty rows, null handling)</li>
 *     <li>Quoting rules (delimiter, newline, quotes, spaces, custom format)</li>
 *     <li>Configuration effects (newline)</li>
 *     <li>Resource / flushing behaviour</li>
 *     <li>Defensive checks (constructor null handling)</li>
 * </ul>
 */
class CsvPrinterTest {

    /**
     * Core, "happy path" behaviour of {@link CsvPrinter}.
     */
    @Nested
    class CoreBehaviour {

        @Test
        void writesSimpleRowWithoutQuoting() throws IOException {
            StringWriter out = new StringWriter();
            CsvFormat fmt = CsvFormat.rfc4180();  // comma, ",", "\n"

            CsvPrinter printer = new CsvPrinter(out, fmt);
            printer.printRow(List.of("a", "b", "c"));
            printer.close();

            assertEquals("a,b,c\n", out.toString());
        }

        /**
         * An empty row (no cells) should still write a newline,
         * with no delimiters or other characters.
         */
        @Test
        void emptyRowWritesJustNewline() throws IOException {
            StringWriter out = new StringWriter();
            CsvFormat fmt = CsvFormat.rfc4180();

            CsvPrinter printer = new CsvPrinter(out, fmt);
            printer.printRow(List.of());   // empty list
            printer.close();

            assertEquals("\n", out.toString());
        }

        /**
         * Null cell values are treated as empty strings.
         */
        @Test
        void treatsNullAsEmptyString() throws IOException {
            StringWriter out = new StringWriter();
            CsvFormat fmt = CsvFormat.rfc4180();

            CsvPrinter printer = new CsvPrinter(out, fmt);
            // Arrays.asList *does* allow null elements
            printer.printRow(Arrays.asList("a", null, "c"));
            printer.close();

            // null becomes empty string between delimiters
            assertEquals("a,,c\n", out.toString());
        }
    }

    /**
     * Tests that exercise the quoting and escaping rules used by {@link CsvPrinter}.
     */
    @Nested
    class QuotingRules {

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

        /**
         * Fields containing a carriage return (\r) should also be quoted.
         * This explicitly exercises the v.indexOf('\r') >= 0 branch.
         */
        @Test
        void quotesFieldContainingCarriageReturn() throws IOException {
            StringWriter out = new StringWriter();
            CsvFormat fmt = CsvFormat.rfc4180();

            CsvPrinter printer = new CsvPrinter(out, fmt);
            printer.printRow(List.of("line1\rline2"));
            printer.close();

            assertEquals("\"line1\rline2\"\n", out.toString());
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

        /**
         * When alwaysQuote is enabled, all fields should be wrapped in quotes
         * regardless of their content.
         */
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

        /**
         * Verifies that CsvPrinter honours the delimiter and quote character
         * specified by CsvFormat, not just the RFC-4180 defaults.
         */
        @Test
        void usesCustomDelimiterAndQuoteChar() throws IOException {
            StringWriter out = new StringWriter();
            CsvFormat fmt = CsvFormat.builder()
                    .delimiter(';')
                    .quoteChar('\'')
                    .build();

            CsvPrinter printer = new CsvPrinter(out, fmt);
            printer.printRow(List.of("a;b", "c'd"));
            printer.close();

            // delimiter ';' should trigger quoting,
            // quoteChar '\'' should be doubled inside the field.
            assertEquals("'a;b';'c''d'\n", out.toString());
        }
    }

    /**
     * Tests that focus on configuration-related behaviour, such as newline handling.
     */
    @Nested
    class ConfigurationBehaviour {

        /**
         * CsvPrinter should use the newline sequence configured in CsvFormat,
         * not assume "\n".
         */
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

    /**
     * Tests for flushing and resource handling behaviour.
     */
    @Nested
    class ResourceAndFlushBehaviour {

        /**
         * Explicitly exercises the flush() method. Other tests call close(),
         * but JaCoCo tracks flush() separately, so this ensures it is covered.
         */
        @Test
        void flushDelegatesToUnderlyingWriter() throws IOException {
            StringWriter out = new StringWriter();
            CsvFormat fmt = CsvFormat.rfc4180();
            CsvPrinter printer = new CsvPrinter(out, fmt);

            printer.printRow(List.of("a", "b"));
            printer.flush();  // just exercising the method

            assertEquals("a,b\n", out.toString());
        }
    }

    /**
     * Defensive programming checks for invalid constructor arguments.
     */
    @Nested
    class DefensiveChecks {

        /**
         * The constructor must reject a null Writer and throw a NullPointerException
         * 
         */
        @Test
        void constructorRejectsNullWriter() {
            CsvFormat fmt = CsvFormat.rfc4180();
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new CsvPrinter(null, fmt)
            );
            assertEquals("out must not be null", ex.getMessage());
        }

        /**
         * The constructor must reject a null CsvFormat and throw a NullPointerException
         * 
         */
        @Test
        void constructorRejectsNullFormat() {
            StringWriter out = new StringWriter();
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new CsvPrinter(out, null)
            );
            assertEquals("fmt must not be null", ex.getMessage());
        }
    }
}
