package com.group5.csv.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CsvFormat configuration class.
 * Tests builder pattern, preset formats, copy-with methods, and validation.
 */
class CsvFormatTest {

    // ========== Preset Format Tests ==========

    @Test
    void testRfc4180DefaultFormat() {
        CsvFormat format = CsvFormat.rfc4180();
        
        assertEquals(',', format.delimiter);
        assertEquals('"', format.quoteChar);
        assertEquals("\n", format.newline);
        assertFalse(format.alwaysQuote);
        assertEquals(CsvFormat.NO_ESCAPE, format.escapeChar);
        assertTrue(format.doubleQuoteEnabled);
        assertFalse(format.allowUnescapedQuotes);
        assertFalse(format.allowUnbalancedQuotes);
        assertFalse(format.trimUnquotedFields);
        assertFalse(format.skipWhitespaceBeforeQuotedField);
    }

    @Test
    void testDefaultConstant() {
        CsvFormat format = CsvFormat.DEFAULT;
        
        assertEquals(',', format.delimiter);
        assertEquals('"', format.quoteChar);
        assertEquals("\n", format.newline);
    }

    @Test
    void testRfc4180WindowsFormat() {
        CsvFormat format = CsvFormat.RFC4180_WINDOWS;
        
        assertEquals(',', format.delimiter);
        assertEquals('"', format.quoteChar);
        assertEquals("\r\n", format.newline);
    }

    @Test
    void testExcelFormat() {
        CsvFormat format = CsvFormat.excel();
        
        assertEquals(',', format.delimiter);
        assertEquals('"', format.quoteChar);
        assertEquals("\r\n", format.newline);
        assertTrue(format.skipWhitespaceBeforeQuotedField);
    }

    @Test
    void testExcelSemicolonFormat() {
        CsvFormat format = CsvFormat.excel_semicolon();
        
        assertEquals(';', format.delimiter);
        assertEquals('"', format.quoteChar);
        assertEquals("\r\n", format.newline);
        assertTrue(format.skipWhitespaceBeforeQuotedField);
    }

    @Test
    void testJsonCsvFormat() {
        CsvFormat format = CsvFormat.json_csv();
        
        assertEquals(',', format.delimiter);
        assertEquals('"', format.quoteChar);
        assertEquals('\\', format.escapeChar);
        assertTrue(format.allowUnescapedQuotes);
        assertTrue(format.allowUnbalancedQuotes);
    }

    @Test
    void testTsvFormat() {
        CsvFormat format = CsvFormat.tsv();
        
        assertEquals('\t', format.delimiter);
        assertEquals(CsvFormat.NO_QUOTE, format.quoteChar);
        assertEquals(CsvFormat.NO_ESCAPE, format.escapeChar);
    }

    // ========== Builder Pattern Tests ==========

    @Test
    void testBuilderDefaultValues() {
        CsvFormat format = CsvFormat.builder().build();
        
        assertEquals(',', format.delimiter);
        assertEquals('"', format.quoteChar);
        assertEquals("\n", format.newline);
        assertFalse(format.alwaysQuote);
    }

    @Test
    void testBuilderWithCustomDelimiter() {
        CsvFormat format = CsvFormat.builder()
                .delimiter(';')
                .build();
        
        assertEquals(';', format.delimiter);
    }

    @Test
    void testBuilderWithCustomQuoteChar() {
        CsvFormat format = CsvFormat.builder()
                .quoteChar('\'')
                .build();
        
        assertEquals('\'', format.quoteChar);
    }

    @Test
    void testBuilderWithCustomNewline() {
        CsvFormat format = CsvFormat.builder()
                .newline("\r\n")
                .build();
        
        assertEquals("\r\n", format.newline);
    }

    @Test
    void testBuilderWithAlwaysQuote() {
        CsvFormat format = CsvFormat.builder()
                .alwaysQuote(true)
                .build();
        
        assertTrue(format.alwaysQuote);
    }

    @Test
    void testBuilderWithEscapeChar() {
        CsvFormat format = CsvFormat.builder()
                .escapeChar('\\')
                .build();
        
        assertEquals('\\', format.escapeChar);
    }

    @Test
    void testBuilderWithDoubleQuoteDisabled() {
        CsvFormat format = CsvFormat.builder()
                .doubleQuoteEnabled(false)
                .build();
        
        assertFalse(format.doubleQuoteEnabled);
    }

    @Test
    void testBuilderWithAllowUnescapedQuotes() {
        CsvFormat format = CsvFormat.builder()
                .allowUnescapedQuotes(true)
                .build();
        
        assertTrue(format.allowUnescapedQuotes);
    }

    @Test
    void testBuilderWithAllowUnbalancedQuotes() {
        CsvFormat format = CsvFormat.builder()
                .allowUnbalancedQuotes(true)
                .build();
        
        assertTrue(format.allowUnbalancedQuotes);
    }

    @Test
    void testBuilderWithTrimUnquotedFields() {
        CsvFormat format = CsvFormat.builder()
                .trimUnquotedFields(true)
                .build();
        
        assertTrue(format.trimUnquotedFields);
    }

    @Test
    void testBuilderWithSkipWhitespace() {
        CsvFormat format = CsvFormat.builder()
                .skipWhitespaceBeforeQuotedField(true)
                .build();
        
        assertTrue(format.skipWhitespaceBeforeQuotedField);
    }

    @Test
    void testBuilderChaining() {
        CsvFormat format = CsvFormat.builder()
                .delimiter('|')
                .quoteChar('\'')
                .newline("\r\n")
                .alwaysQuote(true)
                .escapeChar('\\')
                .build();
        
        assertEquals('|', format.delimiter);
        assertEquals('\'', format.quoteChar);
        assertEquals("\r\n", format.newline);
        assertTrue(format.alwaysQuote);
        assertEquals('\\', format.escapeChar);
    }

    // ========== Copy-With Methods Tests ==========

    @Test
    void testWithDelimiter() {
        CsvFormat original = CsvFormat.DEFAULT;
        CsvFormat modified = original.withDelimiter(';');
        
        assertEquals(';', modified.delimiter);
        assertEquals('"', modified.quoteChar); // unchanged
        assertEquals("\n", modified.newline);  // unchanged
    }

    @Test
    void testWithQuoteChar() {
        CsvFormat original = CsvFormat.DEFAULT;
        CsvFormat modified = original.withQuoteChar('\'');
        
        assertEquals(',', modified.delimiter);  // unchanged
        assertEquals('\'', modified.quoteChar);
        assertEquals("\n", modified.newline);   // unchanged
    }

    @Test
    void testWithNewline() {
        CsvFormat original = CsvFormat.DEFAULT;
        CsvFormat modified = original.withNewline("\r\n");
        
        assertEquals(',', modified.delimiter);   // unchanged
        assertEquals('"', modified.quoteChar);   // unchanged
        assertEquals("\r\n", modified.newline);
    }

    @Test
    void testWithAlwaysQuote() {
        CsvFormat original = CsvFormat.DEFAULT;
        CsvFormat modified = original.withAlwaysQuote(true);
        
        assertEquals(',', modified.delimiter);  // unchanged
        assertTrue(modified.alwaysQuote);
    }

    @Test
    void testImmutability() {
        CsvFormat original = CsvFormat.DEFAULT;
        CsvFormat modified = original.withDelimiter(';');
        
        // Original should be unchanged
        assertEquals(',', original.delimiter);
        // Modified should have new value
        assertEquals(';', modified.delimiter);
        // They should be different objects
        assertNotSame(original, modified);
    }

    // ========== Validation Tests ==========

    @Test
    void testNullNewlineThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CsvFormat.builder().newline(null).build();
        });
        assertTrue(exception.getMessage().contains("newline"));
    }

    @Test
    void testEmptyNewlineThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CsvFormat.builder().newline("").build();
        });
        assertTrue(exception.getMessage().contains("newline"));
    }

    @Test
    void testDelimiterCannotBeCarriageReturn() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CsvFormat.builder().delimiter('\r').build();
        });
        assertTrue(exception.getMessage().contains("delimiter"));
    }

    @Test
    void testDelimiterCannotBeLineFeed() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CsvFormat.builder().delimiter('\n').build();
        });
        assertTrue(exception.getMessage().contains("delimiter"));
    }

    @Test
    void testQuoteCharCannotBeCarriageReturn() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CsvFormat.builder().quoteChar('\r').build();
        });
        assertTrue(exception.getMessage().contains("quoteChar"));
    }

    @Test
    void testQuoteCharCannotBeLineFeed() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CsvFormat.builder().quoteChar('\n').build();
        });
        assertTrue(exception.getMessage().contains("quoteChar"));
    }

    @Test
    void testDelimiterAndQuoteCharMustDiffer() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CsvFormat.builder()
                    .delimiter(',')
                    .quoteChar(',')
                    .build();
        });
        assertTrue(exception.getMessage().contains("delimiter and quoteChar must differ"));
    }

    // ========== Edge Cases ==========

    @Test
    void testTabDelimiter() {
        CsvFormat format = CsvFormat.builder()
                .delimiter('\t')
                .build();
        
        assertEquals('\t', format.delimiter);
    }

    @Test
    void testPipeDelimiter() {
        CsvFormat format = CsvFormat.builder()
                .delimiter('|')
                .build();
        
        assertEquals('|', format.delimiter);
    }

    @Test
    void testSingleQuoteAsQuoteChar() {
        CsvFormat format = CsvFormat.builder()
                .quoteChar('\'')
                .build();
        
        assertEquals('\'', format.quoteChar);
    }

    @Test
    void testNoQuoteChar() {
        CsvFormat format = CsvFormat.builder()
                .quoteChar(CsvFormat.NO_QUOTE)
                .build();
        
        assertEquals(CsvFormat.NO_QUOTE, format.quoteChar);
    }

    @Test
    void testNoEscapeChar() {
        CsvFormat format = CsvFormat.builder()
                .escapeChar(CsvFormat.NO_ESCAPE)
                .build();
        
        assertEquals(CsvFormat.NO_ESCAPE, format.escapeChar);
    }

    // ========== toString() Tests ==========

    @Test
    void testToStringContainsDelimiter() {
        CsvFormat format = CsvFormat.DEFAULT;
        String str = format.toString();
        
        assertTrue(str.contains("delimiter"));
        assertTrue(str.contains("CsvFormat{"));
    }

    @Test
    void testToStringWithTabDelimiter() {
        CsvFormat format = CsvFormat.tsv();
        String str = format.toString();
        
        assertTrue(str.contains("\\t"));
    }

    @Test
    void testToStringWithWindowsNewline() {
        CsvFormat format = CsvFormat.RFC4180_WINDOWS;
        String str = format.toString();
        
        assertTrue(str.contains("\\r\\n"));
    }
}
