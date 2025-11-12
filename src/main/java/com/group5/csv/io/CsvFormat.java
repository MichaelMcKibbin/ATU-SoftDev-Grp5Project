package com.group5.csv.io;

/**
 * CsvFormat is a small, immutable configuration holder that defines
 * *how* CSV files are written or read (the "dialect" of the CSV).
 *
 * In other words — it specifies the grammar rules of the CSV:
 *   - What character separates columns (delimiter)
 *   - What character wraps text values (quoteChar)
 *   - What line ending to use (newline)
 *   - Whether values should always be quoted
 *
 * This class does NOT do any I/O or parsing itself.
 * It is passed into components like CsvPrinter (for writing)
 * or CsvParser (for reading) to ensure consistent behavior
 * across the whole CSV system.
 *
 * Why use this:
 *  • Different tools (Excel, Unix, European locales, etc.) use different separators.
 *  • Separating config from logic makes our code flexible and testable.
 *  • Ensures our reader/writer pair can round-trip values correctly.
 *
 * Typical usage:
 *   CsvFormat format = CsvFormat.rfc4180(); // Default Excel/CSV
 *   CsvPrinter printer = new CsvPrinter(writer, format);
 */

public final class CsvFormat {

    /** The character used to separate columns (e.g. ',' or ';'). */
    public final char delimiter;

    /** The character used to quote text fields that contain commas or newlines. */
    public final char quoteChar;

    /** The newline convention (e.g. "\n" for Unix, "\r\n" for Windows). */
    public final String newline;

    /** If true, every field will be quoted even if not strictly required. */
    public final boolean alwaysQuote;

    /**
     * Private constructor — use the Builder to create an instance.
     * The format object is immutable once built.
     */
    private CsvFormat(char delimiter, char quoteChar, String newline, boolean alwaysQuote) {
        this.delimiter = delimiter;
        this.quoteChar = quoteChar;
        this.newline = newline;
        this.alwaysQuote = alwaysQuote;
    }

    /** Returns a new Builder for constructing a custom format. */
    public static Builder builder() { return new Builder(); }

    /**
     * Builder pattern for creating CsvFormat instances.
     * Avoids huge constructors and provides readable configuration.
     *
     * Example:
     *   CsvFormat format = CsvFormat.builder()
     *       .delimiter(';')
     *       .alwaysQuote(true)
     *       .newline("\r\n")
     *       .build();
     */
    public static final class Builder {
        private char delimiter = ',';        // default comma-separated
        private char quoteChar = '"';        // default double-quote
        private String newline = "\n";       // default Unix newline
        private boolean alwaysQuote = false; // quote only when necessary

        /** Set the column separator character. */
        public Builder delimiter(char d){ this.delimiter = d; return this; }

        /** Set the quote character for text fields. */
        public Builder quoteChar(char q){ this.quoteChar = q; return this; }

        /** Set the newline convention (e.g., "\n" or "\r\n"). */
        public Builder newline(String nl){ this.newline = nl; return this; }

        /** Force all fields to be quoted regardless of content. */
        public Builder alwaysQuote(boolean v){ this.alwaysQuote = v; return this; }

        /** Build and return an immutable CsvFormat. */
        public CsvFormat build(){ return new CsvFormat(delimiter, quoteChar, newline, alwaysQuote); }
    }

    /**
     * Returns a standard RFC-4180 compatible format:
     *  - Comma delimiter
     *  - Double quotes for text
     *  - Unix newlines ("\n")
     *  - Quote only when required
     *
     * This is compatible with Excel, Google Sheets, and most parsers.
     */
    public static CsvFormat rfc4180() { return builder().build(); }

}
