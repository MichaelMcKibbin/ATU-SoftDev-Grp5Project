package com.group5.csv.exceptions;

/**
 * Simple checked exception used by CsvParser for syntax errors in CSV input.
 *
 * If CsvParser does not know the line number it assigns line=-1
 *      than CsvReader will catch the exception and enrich it with the line number.
 */
public class ParseException extends RuntimeException {
    private int line = -1;
    private int column = -1;

    // ----- Getters / Setters -----
    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }
    public int getColumn() { return column; }
    public void setColumn(int column) { this.column = column; }

    // ----- Constructors -----
    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String message, int line, int column) {
        super(message);
        this.line = line;
        this.column = column;
    }

    @Override
    public String getMessage() {
        String superMessage = super.getMessage();
        if (line <= 0)
            return superMessage;
        else if (column <= 0) {
            return superMessage + " [line %d]".formatted(line);
        } else {
            return superMessage + " [line %d, col %d]".formatted(line, column);
        }
    }
}
