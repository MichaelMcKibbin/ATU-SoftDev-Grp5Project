package com.group5.csv.exceptions;

/**
 * Exception representing a CSV parsing error detected by {@code CsvParser}.
 * <p>
 * This exception contains optional line and column information indicating
 * where in the CSV input the error occurred. If the parser does not know
 * the position, the line and column values default to {@code -1}.
 * </p>
 * <p>
 * {@code CsvReader} is expected to catch this exception and annotate it with correct line
 * information, than rethrow it. This allows deferred positional error reporting
 * when the parser lacks line context.
 * </p>
 * <p>
 * This exception is unchecked, allowing parsing to halt immediately when a
 * syntax error is encountered.
 * </p>
 */
public class ParseException extends RuntimeException {
    private long line = -1;
    private long column = -1;

    // ----- Getters / Setters -----
    /**
     * Returns the line number associated with the parsing error.
     * <p>
     * A value of {@code -1} indicates that the line number is unknown.
     * </p>
     *
     * @return the 1-based line number, or {@code -1} if unavailable
     */
    public long getLine() { return line; }

    /**
     * Sets the line number where the parsing error occurred.
     *
     * @param line the 1-based line number, or {@code -1} if unknown
     */
    public void setLine(int line) { this.line = line; }

    /**
     * Sets the line number where the parsing error occurred.
     *
     * @param line the 1-based line number, or {@code -1} if unknown
     */
    public void setLine(long line) { this.line = line; }

    /**
     * Returns the column number associated with the parsing error.
     * <p>
     * A value of {@code -1} indicates that the column number is unknown.
     * </p>
     *
     * @return the 1-based column number, or {@code -1} if unavailable
     */
    public long getColumn() { return column; }

    /**
     * Sets the column number where the parsing error occurred.
     *
     * @param column the 1-based column position, or {@code -1} if unknown
     */
    public void setColumn(int column) { this.column = column; }

    /**
     * Sets the column number where the parsing error occurred.
     *
     * @param column the 1-based column position, or {@code -1} if unknown
     */
    public void setColumn(long column) { this.column = column; }

    // ----- Constructors -----
    /**
     * Creates a new {@code ParseException} with the specified message.
     * Line and column information remain unspecified.
     *
     * @param message a description of the parsing error
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code ParseException} with the specified message and cause.
     * Line and column information remain unspecified.
     *
     * @param message a description of the parsing error
     * @param cause   the underlying cause of the exception
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new {@code ParseException} with a message and positional information.
     *
     * @param message a description of the parsing error
     * @param line    the 1-based line number where the error occurred
     * @param column  the 1-based column number where the error occurred
     */
    public ParseException(String message, int line, int column) {
        this(message, (long)line, (long)column);
    }

    /**
     * Creates a new {@code ParseException} with a message and positional information.
     *
     * @param message a description of the parsing error
     * @param line    the 1-based line number where the error occurred
     * @param column  the 1-based column number where the error occurred
     */
    public ParseException(String message, long line, long column) {
        super(message);
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the formatted error message including line and column information
     * when available. If line and column numbers are unspecified, the original
     * exception message is returned unchanged.
     *
     * @return the detailed error message including position context when known
     */
    @Override
    public String getMessage() {
        String superMessage = super.getMessage();
        if (line <= 0L)
            return superMessage;
        else if (column <= 0L) {
            return superMessage + " [line %d]".formatted(line);
        } else {
            return superMessage + " [line %d, col %d]".formatted(line, column);
        }
    }
}
