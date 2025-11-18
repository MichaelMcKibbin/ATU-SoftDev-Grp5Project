package com.group5.csv.io;

import com.group5.csv.testutils.VirtualReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
    }


    // ------ PARSING ------

    // ------ NEWLINE HANDLING ------

    @ParameterizedTest
    @MethodSource("newlineCases")
    void parsesNewlineVariationsCorrectly(String input, List<List<String>> expected) {
        CsvParser parser = new CsvParser(CsvFormat.DEFAULT, new VirtualReader(input));

        List<List<String>> actual = assertDoesNotThrow(() -> {
            List<List<String>> rows = new ArrayList<>();
            for (List<String> row; (row = parser.readRow()) != null; )
                rows.add(row);
            return rows;
        });

        assertEquals(expected, actual);
    }

    private static List<List<String>> rows(String... values) {
        return Arrays.stream(values)
                .map(List::of)
                .toList();
    }

    private static Stream<Arguments> newlineCases() {
        return Stream.of(
                Arguments.of("a\r\nb\r\nc\r\nd", rows("a","b","c","d")),
                Arguments.of("a\nb\nc\nd",       rows("a","b","c","d")),
                Arguments.of("a\rb\rc\rd",       rows("a","b","c","d")),
                Arguments.of("a\rb\r\nc\nd\n\re", rows("a","b","c","d","","e"))
        );
    }

    // ------ RFC4180 FORMAT ------



}