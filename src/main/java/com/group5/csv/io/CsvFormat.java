package com.group5.csv.io;

/**
 * CsvFormat is a small, immutable configuration holder that defines
 * *how* CSV files are written or read (the "dialect" of the CSV).
 */
public final class CsvFormat {

    /**
     * constant for the case when no escaping is allowed
     */
    public static final char NO_ESCAPE = '\0';

    /**
     * constant for the case when no quoting is allowed
     */
    public static final char NO_QUOTE = '\0';

    /**
     * The character used to separate columns (e.g. ',' or ';').
     */
    public final char delimiter;

    /**
     * The character used to quote text fields that contain commas or newlines.
     */
    public final char quoteChar;

    /**
     * The newline convention (e.g. "\n" for Unix, "\r\n" for Windows).
     */
    public final String newline;

    /**
     * If true, every field will be quoted even if not strictly required.
     */
    public final boolean alwaysQuote;

    /**
     * defines escaping behavior
     */
    public final char escapeChar;

    /**
     * If true, allows a doubled quote sequence ("") inside a quoted field to represent a literal quote.
     */
    public final boolean doubleQuoteEnabled;

    /**
     * If false, quotes inside unquoted fields cause error
     */
    public final boolean allowUnescapedQuotes;

    /**
     * If true, tolerate missing closing quote
     */
    public final boolean allowUnbalancedQuotes;

    /**
     * Trim spaces around unquoted fields if true (when parsing).
     */
    public final boolean trimUnquotedFields;

    /**
     * Excel-like behaviour when parsing fields.
     */
    public final boolean skipWhitespaceBeforeQuotedField;

    /**
     * Default CSV (RFC 4180-ish, LF line endings).
     */
    public static final CsvFormat DEFAULT = rfc4180();

    /**
     * Windows-friendly preset (CRLF).
     */
    public static final CsvFormat RFC4180_WINDOWS =
            builder().newline("\r\n").build();

    // Copy-with helpers
    public CsvFormat withDelimiter(char d) {
        return new CsvFormat(d, quoteChar, newline, alwaysQuote,
                escapeChar, doubleQuoteEnabled, allowUnbalancedQuotes,
                allowUnescapedQuotes, trimUnquotedFields,
                skipWhitespaceBeforeQuotedField);
    }

    public CsvFormat withQuoteChar(char q) {
        return new CsvFormat(delimiter, q, newline, alwaysQuote,
                escapeChar, doubleQuoteEnabled, allowUnbalancedQuotes,
                allowUnescapedQuotes, trimUnquotedFields,
                skipWhitespaceBeforeQuotedField);
    }

    public CsvFormat withNewline(String nl) {
        return new CsvFormat(delimiter, quoteChar, nl, alwaysQuote,
                escapeChar, doubleQuoteEnabled, allowUnbalancedQuotes,
                allowUnescapedQuotes, trimUnquotedFields,
                skipWhitespaceBeforeQuotedField);
    }

    public CsvFormat withAlwaysQuote(boolean v) {
        return new CsvFormat(delimiter, quoteChar, newline, v,
                escapeChar, doubleQuoteEnabled, allowUnbalancedQuotes,
                allowUnescapedQuotes, trimUnquotedFields,
                skipWhitespaceBeforeQuotedField);
    }

    private CsvFormat(char delimiter,
                      char quoteChar,
                      String newline,
                      boolean alwaysQuote,
                      char escapeChar,
                      boolean doubleQuoteEnabled,
                      boolean allowUnbalancedQuotes,
                      boolean allowUnescapedQuotes,
                      boolean trimUnquotedFields,
                      boolean skipWhitespaceBeforeQuotedField) {

        if (newline == null || newline.isEmpty())
            throw new IllegalArgumentException("newline cannot be null/empty");
        if (delimiter == '\r' || delimiter == '\n')
            throw new IllegalArgumentException("delimiter cannot be CR/LF");
        if (quoteChar == '\r' || quoteChar == '\n')
            throw new IllegalArgumentException("quoteChar cannot be CR/LF");
        if (delimiter == quoteChar)
            throw new IllegalArgumentException("delimiter and quoteChar must differ");

        this.delimiter = delimiter;
        this.quoteChar = quoteChar;
        this.newline = newline;
        this.alwaysQuote = alwaysQuote;
        this.escapeChar = escapeChar;
        this.doubleQuoteEnabled = doubleQuoteEnabled;
        this.allowUnbalancedQuotes = allowUnbalancedQuotes;
        this.allowUnescapedQuotes = allowUnescapedQuotes;
        this.trimUnquotedFields = trimUnquotedFields;
        this.skipWhitespaceBeforeQuotedField = skipWhitespaceBeforeQuotedField;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private char delimiter = ',';
        private char quoteChar = '"';
        private String newline = "\n";
        private boolean alwaysQuote = false;

        private char escapeChar = NO_ESCAPE;
        private boolean doubleQuoteEnabled = true;
        private boolean allowUnescapedQuotes = false;
        private boolean allowUnbalancedQuotes = false;
        private boolean trimUnquotedFields = false;
        private boolean skipWhitespaceBeforeQuotedField = false;

        public Builder delimiter(char d) {
            this.delimiter = d;
            return this;
        }

        public Builder quoteChar(char q) {
            this.quoteChar = q;
            return this;
        }

        public Builder newline(String nl) {
            this.newline = nl;
            return this;
        }

        public Builder alwaysQuote(boolean v) {
            this.alwaysQuote = v;
            return this;
        }

        public Builder escapeChar(char c) {
            this.escapeChar = c;
            return this;
        }

        public Builder doubleQuoteEnabled(boolean v) {
            this.doubleQuoteEnabled = v;
            return this;
        }

        public Builder allowUnescapedQuotes(boolean v) {
            this.allowUnescapedQuotes = v;
            return this;
        }

        public Builder allowUnbalancedQuotes(boolean v) {
            this.allowUnbalancedQuotes = v;
            return this;
        }

        public Builder trimUnquotedFields(boolean v) {
            this.trimUnquotedFields = v;
            return this;
        }

        public Builder skipWhitespaceBeforeQuotedField(boolean v) {
            this.skipWhitespaceBeforeQuotedField = v;
            return this;
        }

        public CsvFormat build() {
            return new CsvFormat(
                    delimiter, quoteChar, newline, alwaysQuote,
                    escapeChar, doubleQuoteEnabled,
                    allowUnbalancedQuotes, allowUnescapedQuotes,
                    trimUnquotedFields, skipWhitespaceBeforeQuotedField
            );
        }
    }

    /**
     * RFC-4180 defaults (LF line endings).
     */
    public static CsvFormat rfc4180() {
        return builder().build();
    }

    /**
     * Excel format (CRLF, Excel-style whitespace).
     */
    public static CsvFormat excel() {
        return builder()
                .newline("\r\n")
                .skipWhitespaceBeforeQuotedField(true)
                .build();
    }

    /**
     * Excel with semicolon as delimiter.
     */
    public static CsvFormat excel_semicolon() {
        return builder()
                .newline("\r\n")
                .delimiter(';')
                .skipWhitespaceBeforeQuotedField(true)
                .build();
    }

    /**
     * Lenient, JSON-style CSV (backslash escaping, unbalanced quotes allowed).
     */
    public static CsvFormat json_csv() {
        return builder()
                .escapeChar('\\')
                .allowUnescapedQuotes(true)
                .allowUnbalancedQuotes(true)
                .build();
    }

    /**
     * Tab-separated values, no quoting/escaping.
     */
    public static CsvFormat tsv() {
        return builder()
                .delimiter('\t')
                .quoteChar(NO_QUOTE)
                .escapeChar(NO_ESCAPE)
                .build();
    }

    @Override
    public String toString() {
        return "CsvFormat{" +
                "delimiter=" + printableChar(delimiter) +
                ", quoteChar=" + printableChar(quoteChar) +
                ", escapeChar=" + printableChar(escapeChar) +
                ", newline=" + printableLineSeparator(newline) +
                ", alwaysQuote=" + alwaysQuote +
                ", doubleQuoteEnabled=" + doubleQuoteEnabled +
                ", allowUnescapedQuotes=" + allowUnescapedQuotes +
                ", allowUnbalancedQuotes=" + allowUnbalancedQuotes +
                ", trimUnquotedFields=" + trimUnquotedFields +
                ", skipWhitespaceBeforeQuotedField=" + skipWhitespaceBeforeQuotedField +
                '}';
    }

    private static String printableChar(char c) {
        return switch (c) {
            case '\t' -> "'\\t'";
            case '\n' -> "'\\n'";
            case '\r' -> "'\\r'";
            case '\0' -> "'\\0'";
            default -> "'" + c + "'";
        };
    }

    private static String printableLineSeparator(String sep) {
        return sep.replace("\r", "\\r").replace("\n", "\\n");
    }


//package com.group5.csv.io;
//
///**
// * CsvFormat is a small, immutable configuration holder that defines
// * *how* CSV files are written or read (the "dialect" of the CSV).
// *
// * In other words — it specifies the grammar rules of the CSV:
// *   - What character separates columns (delimiter)
// *   - What character wraps text values (quoteChar)
// *   - What line ending to use (newline)
// *   - Whether values should always be quoted
// *
// * This class does NOT do any I/O or parsing itself.
// * It is passed into components like CsvPrinter (for writing)
// * or CsvParser (for reading) to ensure consistent behavior
// * across the whole CSV system.
// *
// * Why use this:
// *  • Different tools (Excel, Unix, European locales, etc.) use different separators.
// *  • Separating config from logic makes our code flexible and testable.
// *  • Ensures our reader/writer pair can round-trip values correctly.
// *
// * Typical usage:
// *   CsvFormat format = CsvFormat.rfc4180(); // Default Excel/CSV
// *   CsvPrinter printer = new CsvPrinter(writer, format);
// */
//
//public final class CsvFormat {
//
//    /** constant for the case when no escaping is allowed */
//    public static final char NO_ESCAPE = '\0';
//
//    /** Constant for the case when no quoting is allowed */
//    public static final char NO_QUOTE  = '\0';
//
//    /** The character used to separate columns (e.g. ',' or ';'). */
//    public final char delimiter;
//
//    /** The character used to quote text fields that contain commas or newlines. */
//    public final char quoteChar;
//
//    /** defines escaping behavior */
//    public final char escapeChar;
//
////    /** Reserved for lenient mode when both " and / are used for escaping */
////    public final char escapeChar2;
//
//    /** If true, allows a doubled quote sequence ("") inside a quoted field to represent a literal quote. */
//    public final boolean doubleQuoteEnabled;
//
//    /** If false, quotes inside unquoted fields cause error */
//    public final boolean allowUnescapedQuotes;
//
//    /** If true, tolerate missing closing quote */
//    public final boolean allowUnbalancedQuotes;
//
//    /** Trim spaces around unquoted fields if true (when parsing) */
//    public final boolean trimUnquotedFields;
//
//    /** Excel-like behaviour when parsing fields  */
//    public final boolean skipWhitespaceBeforeQuotedField;
//
//    /** The newline convention (e.g. "\n" for Unix, "\r\n" for Windows).
//     * effects CsvPrinter, in CsvParser any newline sequence is acceptable */
//    public final String newline;
//
//    /** If true, every field will be quoted even if not strictly required.
//     * effects CsvPrinter only */
//    public final boolean alwaysQuote;
//
//
//    /**
//     * Private constructor — use the Builder to create an instance.
//     * The format object is immutable once built.
//     */
//    private CsvFormat(char delimiter, char quoteChar, String newline, boolean alwaysQuote,
//                      char escapeChar, boolean doubleQuoteEnabled, boolean allowUnbalancedQuotes,
//                      boolean  allowUnescapedQuotes, boolean trimUnquotedFields,
//                      boolean skipWhitespaceBeforeQuotedField)
//    {
//        // simple validation
//        if (newline == null || newline.isEmpty())
//            throw new IllegalArgumentException("newline cannot be null/empty");
//        if (delimiter == '\r' || delimiter == '\n')
//            throw new IllegalArgumentException("delimiter cannot be CR/LF");
//        if (quoteChar == '\r' || quoteChar == '\n')
//            throw new IllegalArgumentException("quoteChar cannot be CR/LF");
//        if (delimiter == quoteChar)
//            throw new IllegalArgumentException("delimiter and quoteChar must differ");
//
//        this.delimiter = delimiter;
//        this.quoteChar = quoteChar;
//        this.newline = newline;
//        this.alwaysQuote = alwaysQuote;
//        this.escapeChar = escapeChar;
//        this.allowUnbalancedQuotes = allowUnbalancedQuotes;
//        this.allowUnescapedQuotes = allowUnescapedQuotes;
//        this.doubleQuoteEnabled = doubleQuoteEnabled;
//        this.trimUnquotedFields = trimUnquotedFields;
//        this.skipWhitespaceBeforeQuotedField = skipWhitespaceBeforeQuotedField;
//    }
//
//    /** Returns a new Builder for constructing a custom format. */
//    public static Builder builder() { return new Builder(); }
//
//    /**
//     * Builder pattern for creating CsvFormat instances.
//     * Avoids huge constructors and provides readable configuration.
//     *
//     * Example:
//     *   CsvFormat format = CsvFormat.builder()
//     *       .delimiter(';')
//     *       .alwaysQuote(true)
//     *       .newline("\r\n")
//     *       .build();
//     */
//    public static final class Builder {
//        private char delimiter = ',';        // default comma-separated
//        private char quoteChar = '"';        // default double-quote
//        private String newline = "\n";       // default Unix newline
//        private boolean alwaysQuote = false; // quote only when necessary
//
//        private char escapeChar = NO_ESCAPE;            // default no escape
////        private char escapeChar2 = NO_ESCAPE;
//        private boolean doubleQuoteEnabled = true;      // default enabled
//        private boolean allowUnescapedQuotes = false;   // false for RFC, true for Excel
//        private boolean allowUnbalancedQuotes = false;
//        private boolean trimUnquotedFields = false;
//        private boolean skipWhitespaceBeforeQuotedField = false;    // true for Excel only
//
//        /** Set the column separator character. */
//        public Builder delimiter(char d){ this.delimiter = d; return this; }
//
//        /** Set the quote character for text fields. */
//        public Builder quoteChar(char q){ this.quoteChar = q; return this; }
//
//        /** Set the newline convention (e.g., "\n" or "\r\n"). */
//        public Builder newline(String nl){ this.newline = nl; return this; }
//
//        /** Force all fields to be quoted regardless of content. */
//        public Builder alwaysQuote(boolean v){ this.alwaysQuote = v; return this; }
//
//        public Builder escapeChar(char escapeChar) {
//            this.escapeChar = escapeChar; return this;
//        }
//
//        public Builder doubleQuoteEnabled(boolean doubleQuoteEnabled) {
//            this.doubleQuoteEnabled = doubleQuoteEnabled; return this;
//        }
//
//        public Builder allowUnescapedQuotes(boolean allowUnescapedQuotes) {
//            this.allowUnescapedQuotes = allowUnescapedQuotes; return this;
//        }
//
//        public Builder allowUnbalancedQuotes(boolean allowUnbalancedQuotes) {
//            this.allowUnbalancedQuotes = allowUnbalancedQuotes; return this;
//        }
//
//        public Builder skipWhitespaceBeforeQuotedField(boolean skipWhitespaceBeforeQuotedField) {
//            this.skipWhitespaceBeforeQuotedField = skipWhitespaceBeforeQuotedField; return this;
//        }
//
//        public Builder trimUnquotedFields(boolean trimUnquotedFields) {
//            this.trimUnquotedFields = trimUnquotedFields; return this;
//        }
//
//        /** Build and return an immutable CsvFormat. */
//        public CsvFormat build(){ return new CsvFormat(delimiter, quoteChar, newline, alwaysQuote, escapeChar, doubleQuoteEnabled, allowUnbalancedQuotes, allowUnescapedQuotes, trimUnquotedFields, skipWhitespaceBeforeQuotedField); }
//    }
//
//    /**
//     * Returns a standard RFC-4180 compatible format:
//     *  - Comma delimiter
//     *  - Double quotes for text
//     *  - Unix newlines ("\n")
//     *  - Quote only when required
//     *
//     * This is compatible with RFC-4180
//     */
//    public static CsvFormat rfc4180() { return builder().build(); }
//
//    /** Excel format */
//    public static CsvFormat excel() {
//        return builder()
//                .newline("\r\n")
//                .skipWhitespaceBeforeQuotedField(true)
//                .build();
//    }
//
//    /** Excel with semicolon as a  delimiter */
//    public static CsvFormat excel_semicolon() {
//        return builder()
//                .newline("\r\n")
//                .delimiter(';')
//                .skipWhitespaceBeforeQuotedField(true)
//                .build();
//    }
//
//    /** Lenient interpretation of CSV that used in JavaScript, Node.js, or JSON-based web tools */
//    public static CsvFormat json_csv() {
//        return builder()
//                .newline("\n")
//                .delimiter(',')
//                .escapeChar('\\')
//                .doubleQuoteEnabled(false)
//                .trimUnquotedFields(true)
//                .skipWhitespaceBeforeQuotedField(true)
//                .allowUnescapedQuotes(true)
//                .allowUnbalancedQuotes(true)
//                .build();
//    }
//
//    /** Tab Separated values */
//    public static CsvFormat tsv() {
//        return builder()
//                .newline("\n")
//                .delimiter('\t')
//                .escapeChar(NO_ESCAPE)
//                .quoteChar(NO_QUOTE)
//                .doubleQuoteEnabled(false)
//                .trimUnquotedFields(false)
//                .skipWhitespaceBeforeQuotedField(false)
//                .allowUnescapedQuotes(true)
//                .allowUnbalancedQuotes(true)
//                .build();
//    }
//
//    // --- Utility ---
//    @Override
//    public String toString() {
//        return "CsvFormat{" +
//                "delimiter=" + printableChar(delimiter) +
//                ", quoteChar=" + printableChar(quoteChar) +
//                ", escapeChar=" + printableChar(escapeChar) +
////                ", escapeChar2=" + printableChar(escapeChar2) +
//                ", doubleQuoteEnabled=" + doubleQuoteEnabled +
//                ", trimUnquotedFields=" + trimUnquotedFields +
//                ", skipWhitespaceBeforeQuotedField=" + skipWhitespaceBeforeQuotedField +
//                ", allowUnescapedQuotes=" + allowUnescapedQuotes +
//                ", allowUnbalancedQuotes=" + allowUnbalancedQuotes +
//                ", alwaysQuote=" + alwaysQuote +
//                ", newline=" + printableLineSeparator(newline) +
//                '}';
//    }
//
//    // Helper function for internal use in toString()
//    private static String printableChar(char c) {
//        return switch (c) {
//            case '\t' -> "'\\t'";
//            case '\n' -> "'\\n'";
//            case '\r' -> "'\\r'";
//            case '\0' -> "'\\0'";
//            default -> "'" + c + "'";
//        };
//    }
//
//    // Helper function for internal use in toString()
//    private static String printableLineSeparator(String sep) {
//        return sep.replace("\r", "\\r").replace("\n", "\\n");
//    }
//}
}