package com.group5.csv.io;

import com.group5.csv.exceptions.ParseException;
import com.group5.csv.testutils.VirtualReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CsvParser class.
 */
public class CsvParserTest {

    // ------ CONSTRUCTOR ------

    @Test
    void constructorThrowsWhenReaderIsNull() {
        CsvFormat format = CsvFormat.DEFAULT;

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> new CsvParser(format, null));

        assertTrue(ex.getMessage().toLowerCase().contains("reader"), "Expected error about reader");
    }

    @Test
    void constructorThrowsWhenFormatIsNull() {
        Reader reader = new VirtualReader("");
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> new CsvParser(null, reader));
        assertTrue(ex.getMessage().toLowerCase().contains("format"), "Expected error about format");
    }

    @Test
    void constructorThrowsWhenParamsAreNull() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> new CsvParser(null, null));
    }

    @Test
    void constructorSucceedsWithValidArguments() {
        CsvFormat format = CsvFormat.DEFAULT;
        Reader reader = new VirtualReader("");
        CsvParser parser = assertDoesNotThrow(() -> new CsvParser(format, reader));
        assertNotNull(parser);
        assertEquals(parser.getFormat(), format);
    }


    // ------ GENERAL HELPER FUNCTIONS ------

    private static List<List<String>> parseAllRows(CsvParser parser) {
        return assertDoesNotThrow(() -> {
            List<List<String>> result = new ArrayList<>();
            for (List<String> row; (row = parser.readRow()) != null; )
                result.add(row);
            return result;
        });
    }

    private static Stream<Arguments> newlineValidCases() {
        return Stream.of(
                // LF
                Arguments.of("a\nb\nc\nd", List.of(
                        List.of("a"), List.of("b"), List.of("c"), List.of("d"))
                ),

                // CRLF
                Arguments.of("a\r\nb\r\nc\r\nd", List.of(
                        List.of("a"), List.of("b"), List.of("c"), List.of("d"))
                ),

                // CR
                Arguments.of("a\rb\rc\rd", List.of(
                        List.of("a"), List.of("b"), List.of("c"), List.of("d"))
                ),

                // MIXED
                Arguments.of("a\rb\r\nc\nd\n\re", List.of(
                        List.of("a"), List.of("b"), List.of("c"),
                        List.of("d"), List.of(""), List.of("e"))
                )
        );
    }

    // parametrized stream for EOF test with variable delimiter
    private static Stream<Arguments> EOFValidCases(char d) {
        return Stream.of(
                // Empty File -> empty List
                Arguments.of("", List.of()),

                // EOF after LF with no content
                Arguments.of("\n", List.of(List.of(""))),

                // EOF after CRLF with no content
                Arguments.of("\r\n", List.of(List.of(""))),

                // EOF after CR with no content
                Arguments.of("\r", List.of(List.of(""))),

                // EOF after delimiter
                Arguments.of("a%cb%c".formatted(d, d), List.of(List.of("a", "b", ""))),

                // EOF after LF
                Arguments.of("a%cb\n".formatted(d), List.of(List.of("a", "b"))),

                // EOF after CR
                Arguments.of("a%cb\r".formatted(d), List.of(List.of("a", "b"))),

                // EOF after CRLF
                Arguments.of("a%cb\r\n".formatted(d), List.of(List.of("a", "b"))),

                // EOF after Quoted Cell
                Arguments.of("a%cb\n\"c\"".formatted(d), List.of(List.of("a", "b"), List.of("c"))));
    }

    private static Stream<Arguments> semicolonEOFValidCases() {
        return EOFValidCases(';');
    }

    private static Stream<Arguments> commaEOFValidCases() {
        return EOFValidCases(',');
    }

    // parametrized stream for valid cases with variable delimiter
    private static Stream<Arguments> generalValidCases(char d) {
        return Stream.of(
                // Handles delimiters
                Arguments.of(
                        "a%cb%cc\nd%ce".formatted(d, d, d),
                        List.of(List.of("a","b","c"), List.of("d","e"))
                ),

                // Correctly unquote quoted cells
                Arguments.of(
                        "\"a\"%cb%c\"cde\"".formatted(d, d),
                        List.of(List.of("a","b","cde"))
                ),

                // Preserves whitespaces inside quoted cells
                Arguments.of(
                        "\"a \"%c\"b\"%c\" c\"%n\"d  \"%c\" e \"".formatted(d, d, d),
                        List.of(
                                List.of("a ","b"," c"),
                                List.of("d  "," e ")
                        )
                ),

                // Preserves delimiters inside quoted cell
                Arguments.of(
                        "a%c\"b%cc\"\n\"d %ce \"".formatted(d, d, d),
                        List.of(
                                List.of("a","b%cc".formatted(d)),
                                List.of("d %ce ".formatted(d))
                        )
                )
        );
    }

    private static Stream<Arguments> commaDelimiterCases() {
        return generalValidCases(',');
    }

    private static Stream<Arguments> semicolonDelimiterCases() {
        return generalValidCases(';');
    }

    private static Stream<Arguments> tabDelimiterCases() {
        return generalValidCases('\t');
    }

    private static Stream<Arguments> letterDelimiterCases() {
        return generalValidCases('f');
    }

    private static Stream<Arguments> digitDelimiterCases() {
        return generalValidCases('5');
    }

    // applicable if CvsFormat.skipWhitespaceAroundQuotes is false (e.g: RFC-4180)
    private static Stream<Arguments> invalidWhitespaceAroundQuotedCellCases() {
        return Stream.of(
                Arguments.of(" \"abc\",d"),      // Space Before Quoted Cell
                Arguments.of("   \"abc\",d"),    // Spaces Before Quoted Cell
                Arguments.of("\t\"abc\",d"),     // Tab Before Quoted Cell
                Arguments.of("\t \t\"abc\",d"),  // Mixed Whitespaces Before Quoted Cell

                Arguments.of("\"abc\" ,d"),      // Space After Quoted Cell
                Arguments.of("\"abc\"   ,d"),    // Spaces After Quoted Cell
                Arguments.of("\"abc\"\t,d"),     // Tab After Quoted Cell
                Arguments.of("\"abc\"\t \t,d")   // Mixed Whitespaces After Quoted Cell
        );
    }

    // applicable if CvsFormat.skipWhitespaceAroundQuotes is true (e.g: Excel)
    private static Stream<Arguments> skipWhitespaceAroundQuotedCellCases(char d) {
        return Stream.of(
                // No spaces case
                Arguments.of("\"abc\"%cd".formatted(d), List.of(List.of("abc", "d"))),

                // Space Before Quoted Cell
                Arguments.of(" \"abc\"%cd".formatted(d), List.of(List.of("abc", "d"))),

                // Spaces Before Quoted Cell
                Arguments.of("   \"abc\"%cd".formatted(d), List.of(List.of("abc", "d"))),

                // Tab Before Quoted Cell
                Arguments.of("\t\"abc\"%cd".formatted(d), List.of(List.of("abc", "d"))),

                // Mixed Whitespaces Before Quoted Cell
                Arguments.of("\t \t\"abc\"%cd".formatted(d), List.of(List.of("abc", "d"))),

                // Space After Quoted Cell
                Arguments.of("\"abc\" %cd".formatted(d), List.of(List.of("abc", "d"))),

                // Spaces After Quoted Cell
                Arguments.of("\"abc\"   %cd".formatted(d), List.of(List.of("abc", "d"))),

                // Tab After Quoted Cell
                Arguments.of("\"abc\"\t%cd".formatted(d), List.of(List.of("abc", "d"))),

                // Mixed Whitespaces After Quoted Cell
                Arguments.of("\"abc\"\t \t%cd".formatted(d), List.of(List.of("abc", "d")))
        );
    }

    private static Stream<Arguments> skipWhitespaceAroundQuotesComma() {
        return skipWhitespaceAroundQuotedCellCases(',');
    }

    private static Stream<Arguments> skipWhitespaceAroundQuotesSemicolon() {
        return skipWhitespaceAroundQuotedCellCases(';');
    }


    // ------ RFC4180 FORMAT ------

    private static CsvParser rfc4180Parser(String input) {
        return new CsvParser(CsvFormat.rfc4180(), new VirtualReader(input));
    }

    @ParameterizedTest
    @MethodSource("newlineValidCases")
    void rfc4180ParsesNewlineCorrectly(String input, List<List<String>> expected) {
        CsvParser parser = rfc4180Parser(input);
        assertEquals(expected, parseAllRows(parser));
    }

    @ParameterizedTest
    @MethodSource("commaEOFValidCases")
    void rfc4180ParsesEOFCorrectly(String input, List<List<String>> expected) {
        CsvParser parser = rfc4180Parser(input);
        assertEquals(expected, parseAllRows(parser));
    }

    @ParameterizedTest
    @MethodSource("invalidWhitespaceAroundQuotedCellCases")
    void rfc4180RejectsInvalidWhitespaces(String input) {
        CsvParser parser = rfc4180Parser(input);
        ParseException ex = assertThrows(ParseException.class, parser::readRow);
        assertTrue(
                ex.getMessage().toLowerCase().contains("unexpected") ||
                        ex.getMessage().toLowerCase().contains("quote"),
                "Expected an error about unexpected characters or quote handling"
        );
    }

    @ParameterizedTest
    @MethodSource("commaDelimiterCases")
    void rfc4180ParsesValidCells(String input, List<List<String>> expected) {
        CsvParser parser = rfc4180Parser(input);
        assertEquals(expected, parseAllRows(parser));
    }

    @Test
    void rfc4180ThrowsOnUnescapedQuoteInTheEnd() {
        CsvParser parser = rfc4180Parser("ab\"");
        ParseException ex = assertThrows(ParseException.class, parser::readRow);
        assertTrue(ex.getMessage().toLowerCase().contains("quote"));
    }

    @Test
    void rfc4180ThrowsOnUnescapedQuoteInTheMiddle() {
        CsvParser parser = rfc4180Parser("ab\"c");
        ParseException ex = assertThrows(ParseException.class, parser::readRow);
        assertTrue(ex.getMessage().toLowerCase().contains("quote"));
    }

    @Test
    void rfc4180ThrowsOnEOFInsideQuotedCell() {
        CsvParser parser = rfc4180Parser("a,\"c");
        ParseException ex = assertThrows(ParseException.class, parser::readRow);
        assertTrue(ex.getMessage().toLowerCase().contains("quote"));
    }


    // ------ EXCEL FORMAT ------

    private static CsvParser excelParser(String input) {
        return new CsvParser(CsvFormat.excel(), new VirtualReader(input));
    }

    @ParameterizedTest
    @MethodSource("newlineValidCases")
    void excelParsesNewlineCorrectly(String input, List<List<String>> expected) {
        CsvParser parser = excelParser(input);
        assertEquals(expected, parseAllRows(parser));
    }

    @ParameterizedTest
    @MethodSource("commaEOFValidCases")
    void excelParsesEOFCorrectly(String input, List<List<String>> expected) {
        CsvParser parser = excelParser(input);
        assertEquals(expected, parseAllRows(parser));
    }

    @ParameterizedTest
    @MethodSource("commaDelimiterCases")
    void excelParsesValidCells(String input, List<List<String>> expected) {
        CsvParser parser = excelParser(input);
        assertEquals(expected, parseAllRows(parser));
    }

    @ParameterizedTest
    @MethodSource("skipWhitespaceAroundQuotesComma")
    void excelSkipWhitespacesAroundQuotedCell(String input, List<List<String>> expected) {
        CsvParser parser = excelParser(input);
        assertEquals(expected, parseAllRows(parser));
    }

    @Test
    void excelAcceptsUnescapedQuoteInTheEnd() {
        CsvParser parser = excelParser("ab\"");
        List<List<String>> expected = List.of(List.of("ab\"")); // treated as unquoted
        assertEquals(expected, parseAllRows(parser));
    }

    @Test
    void excelAcceptsUnescapedQuoteInTheMiddle() {
        CsvParser parser = excelParser("ab\"c");
        List<List<String>> expected = List.of(List.of("ab\"c")); // treated as unquoted
        assertEquals(expected, parseAllRows(parser));
    }

    @Test
    void excelThrowsOnEOFInsideQuotedCell() {
        CsvParser parser = excelParser("a,\"c");
        ParseException ex = assertThrows(ParseException.class, parser::readRow);
        assertTrue(ex.getMessage().toLowerCase().contains("quote"));
    }


    // ------ EXCEL WITH SEMICOLON FORMAT ------

    private static CsvParser excelSemicolonParser(String input) {
        return new CsvParser(CsvFormat.excel_semicolon(), new VirtualReader(input));
    }

    @ParameterizedTest
    @MethodSource("newlineValidCases")
    void excelSemicolonParsesNewlineCorrectly(String input, List<List<String>> expected) {
        CsvParser parser = excelSemicolonParser(input);
        assertEquals(expected, parseAllRows(parser));
    }

    @ParameterizedTest
    @MethodSource("semicolonEOFValidCases")
    void excelsemicolonParsesEOFCorrectly(String input, List<List<String>> expected) {
        CsvParser parser = excelSemicolonParser(input);
        assertEquals(expected, parseAllRows(parser));
    }

    @ParameterizedTest
    @MethodSource("semicolonDelimiterCases")
    void excelSemicolonParsesValidCells(String input, List<List<String>> expected) {
        CsvParser parser = excelSemicolonParser(input);
        assertEquals(expected, parseAllRows(parser));
    }

    @ParameterizedTest
    @MethodSource("skipWhitespaceAroundQuotesSemicolon")
    void excelSemicolonSkipWhitespacesAroundQuotedCell(String input, List<List<String>> expected) {
        CsvParser parser = excelSemicolonParser(input);
        assertEquals(expected, parseAllRows(parser));
    }

    @Test
    void excelSemicolonAcceptsUnescapedQuoteInTheEnd() {
        CsvParser parser = excelSemicolonParser("ab\"");
        List<List<String>> expected = List.of(List.of("ab\"")); // treated as unquoted
        assertEquals(expected, parseAllRows(parser));
    }

    @Test
    void excelSemicolonAcceptsUnescapedQuoteInTheMiddle() {
        CsvParser parser = excelSemicolonParser("ab\"c");
        List<List<String>> expected = List.of(List.of("ab\"c")); // treated as unquoted
        assertEquals(expected, parseAllRows(parser));
    }

    @Test
    void excelSemicolonThrowsOnEOFInsideQuotedCell() {
        CsvParser parser = excelSemicolonParser("a;\"c");
        ParseException ex = assertThrows(ParseException.class, parser::readRow);
        assertTrue(ex.getMessage().toLowerCase().contains("quote"));
    }

}