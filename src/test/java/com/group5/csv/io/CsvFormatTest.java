package com.group5.csv.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvFormatTest {

    @Test
    void testDefaultRfc4180Format() {
        CsvFormat fmt = CsvFormat.rfc4180();

        assertEquals(',', fmt.delimiter);
        assertEquals('"', fmt.quoteChar);
        assertEquals("\n", fmt.newline);
        assertFalse(fmt.alwaysQuote);
        assertEquals(CsvFormat.NO_ESCAPE, fmt.escapeChar);
        assertTrue(fmt.doubleQuoteEnabled);
        assertFalse(fmt.allowUnescapedQuotes);
        assertFalse(fmt.allowUnbalancedQuotes);
        assertFalse(fmt.trimUnquotedFields);
        assertFalse(fmt.skipWhitespaceBeforeQuotedField);
    }

    @Test
    void testWithDelimiter() {
        CsvFormat fmt = CsvFormat.rfc4180().withDelimiter(';');
        assertEquals(';', fmt.delimiter);
    }

    @Test
    void testWithQuoteChar() {
        CsvFormat fmt = CsvFormat.rfc4180().withQuoteChar('\'');
        assertEquals('\'', fmt.quoteChar);
    }

    @Test
    void testWithNewline() {
        CsvFormat fmt = CsvFormat.rfc4180().withNewline("\r\n");
        assertEquals("\r\n", fmt.newline);
    }

    @Test
    void testWithAlwaysQuote() {
        CsvFormat fmt = CsvFormat.rfc4180().withAlwaysQuote(true);
        assertTrue(fmt.alwaysQuote);
    }

    @Test
    void testExcelPreset() {
        CsvFormat fmt = CsvFormat.excel();

        assertEquals("\r\n", fmt.newline);
        assertTrue(fmt.skipWhitespaceBeforeQuotedField);
    }

    @Test
    void testExcelSemicolonPreset() {
        CsvFormat fmt = CsvFormat.excel_semicolon();

        assertEquals(';', fmt.delimiter);
        assertEquals("\r\n", fmt.newline);
        assertTrue(fmt.skipWhitespaceBeforeQuotedField);
    }

    @Test
    void testJsonCsvPreset() {
        CsvFormat fmt = CsvFormat.json_csv();

        assertEquals('\\', fmt.escapeChar);
        assertTrue(fmt.allowUnescapedQuotes);
        assertTrue(fmt.allowUnbalancedQuotes);
    }

    @Test
    void testTsvPreset() {
        CsvFormat fmt = CsvFormat.tsv();

        assertEquals('\t', fmt.delimiter);
        assertEquals(CsvFormat.NO_QUOTE, fmt.quoteChar);
        assertEquals(CsvFormat.NO_ESCAPE, fmt.escapeChar);
    }

    @Test
    void testInvalidNewlineThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                CsvFormat.builder().newline("").build()
        );
    }

    @Test
    void testInvalidDelimiterThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                CsvFormat.builder().delimiter('\n').build()
        );
    }

    @Test
    void testDelimiterAndQuoteSameThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                CsvFormat.builder().delimiter('x').quoteChar('x').build()
        );
    }
}
