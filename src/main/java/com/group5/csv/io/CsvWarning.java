package com.group5.csv.io;

/**
 * Non-fatal issue detected while parsing a CSV row.
 * Returned by {@link CsvReader#getLastRowWarning()} immediately after
 * {@link CsvReader#readRow()}.
 */
public final class CsvWarning {

    /** Category of structural row problems. */
    public enum Type {
        /** Row has fewer fields than expected (missing fields padded). */
        TOO_FEW_FIELDS,
        /** Row has more fields than expected (extra fields discarded). */
        TOO_MANY_FIELDS
    }

    private final long line;
    private final Type type;
    private final String message;

    /**
     * Creates a warning for a specific row.
     *
     * @param line    1-based row number
     * @param type    warning type
     * @param message short description
     */
    public CsvWarning(long line, Type type, String message) {
        this.line = line;
        this.type = type;
        this.message = message;
    }

    /** Line where the issue occurred. */
    public long line() { return line; }

    /** Warning category. */
    public Type type() { return type; }

    /** Human-readable description. */
    public String message() { return message; }

    @Override
    public String toString() {
        return "CsvWarning{line=" + line +
                ", type=" + type +
                ", message='" + message + "'}";
    }
}
