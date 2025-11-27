package com.group5.csv.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvExceptionTest {
    @Test
    void setsMessageAndCause() {
        String message = "Parsing failed";
        Throwable cause = new IllegalArgumentException("Invalid input");
        CsvException ex = new CsvException(message, cause);

        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void getMessageCorrectly() {
        String msg = "Parsing failed";
        CsvException ex = new CsvException(msg);
        assertEquals(msg, ex.getMessage());
    }
}