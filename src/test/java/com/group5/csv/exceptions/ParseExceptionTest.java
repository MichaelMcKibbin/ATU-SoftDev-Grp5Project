package com.group5.csv.exceptions;

import com.group5.csv.io.CsvFormat;
import com.group5.csv.io.CsvParser;
import com.group5.csv.testutils.VirtualReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParseExceptionTest {

    @Test
    void setsMessageAndCause() {
        String message = "Parsing failed";
        Throwable cause = new IllegalArgumentException("Invalid input");
        ParseException ex = new ParseException(message, cause);

        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void getMessageCorrectly() {
        String msg = "Parsing failed";
        ParseException ex = new ParseException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void getLineInMessage() {
        String msg = "Parsing failed";
        ParseException ex = new ParseException(msg, 1_000_000_007, -4);
        assertTrue(ex.getMessage().toLowerCase().contains("line")
                && ex.getMessage().toLowerCase().contains("" + 1_000_000_007));
    }

    @Test
    void getColInMessage() {
        String msg = "Parsing failed";
        ParseException ex = new ParseException(msg, 1, 1_000_000_007);
        assertTrue(ex.getMessage().toLowerCase().contains("col")
                && ex.getMessage().toLowerCase().contains("" + 1_000_000_007));
    }

    @Test
    void getLineCorrectly() {
        ParseException ex = new ParseException("Parsing failed", 1_000_000_007, 0);
        assertEquals(1_000_000_007, ex.getLine());
    }

    @Test
    void setLineCorrectly() {
        ParseException ex = new ParseException("Parsing failed", -1, 0);
        ex.setLine(1_000_000_007);
        assertEquals(1_000_000_007, ex.getLine());
    }

    @Test
    void getColumnCorrectly() {
        ParseException ex = new ParseException("Parsing failed", 0, 1_000_000_007);
        assertEquals(1_000_000_007, ex.getColumn());
    }

    @Test
    void setColumnCorrectly() {
        ParseException ex = new ParseException("Parsing failed", -1, -1);
        ex.setColumn(1_000_000_007);
        assertEquals(1_000_000_007, ex.getColumn());
    }

    @Test
    void correctlyTracksPositionInMultilineFile() {
        CsvParser parser = new CsvParser(CsvFormat.rfc4180(), new VirtualReader("aaaaaaaaaa\nb,\"c"));
        ParseException ex = assertThrows(ParseException.class, () -> {
            List<List<String>> result = new ArrayList<>();
            for (List<String> row; (row = parser.readRow()) != null; )
                result.add(row);
        });
        assertEquals(5, ex.getColumn());
        assertTrue(ex.getMessage().toLowerCase().contains("quote"));
    }
}