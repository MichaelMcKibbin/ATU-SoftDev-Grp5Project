package com.group5.csv.exceptions;

/**
 * Simple checked exception used by CsvParser for syntax errors in CSV input.
 */
public class ParseException extends RuntimeException {
    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
