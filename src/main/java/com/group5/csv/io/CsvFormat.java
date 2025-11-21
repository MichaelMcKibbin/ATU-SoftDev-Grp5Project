package com.group5.csv.io;

import java.util.Objects;

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
    public final boolean skipWhitespaceAroundQuotes;

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
                skipWhitespaceAroundQuotes);
    }

    public CsvFormat withQuoteChar(char q) {
        return new CsvFormat(delimiter, q, newline, alwaysQuote,
                escapeChar, doubleQuoteEnabled, allowUnbalancedQuotes,
                allowUnescapedQuotes, trimUnquotedFields,
                skipWhitespaceAroundQuotes);
    }

    public CsvFormat withNewline(String nl) {
        return new CsvFormat(delimiter, quoteChar, nl, alwaysQuote,
                escapeChar, doubleQuoteEnabled, allowUnbalancedQuotes,
                allowUnescapedQuotes, trimUnquotedFields,
                skipWhitespaceAroundQuotes);
    }

    public CsvFormat withAlwaysQuote(boolean v) {
        return new CsvFormat(delimiter, quoteChar, newline, v,
                escapeChar, doubleQuoteEnabled, allowUnbalancedQuotes,
                allowUnescapedQuotes, trimUnquotedFields,
                skipWhitespaceAroundQuotes);
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
                      boolean skipWhitespaceAroundQuotes) {

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
        this.skipWhitespaceAroundQuotes = skipWhitespaceAroundQuotes;
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
        private boolean skipWhitespaceAroundQuotes = false;

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

        public Builder skipWhitespaceAroundQuotes(boolean v) {
            this.skipWhitespaceAroundQuotes = v;
            return this;
        }

        public CsvFormat build() {
            return new CsvFormat(
                    delimiter, quoteChar, newline, alwaysQuote,
                    escapeChar, doubleQuoteEnabled,
                    allowUnbalancedQuotes, allowUnescapedQuotes,
                    trimUnquotedFields, skipWhitespaceAroundQuotes
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
                .skipWhitespaceAroundQuotes(true)
                .allowUnescapedQuotes(true)
                .build();
    }

    /**
     * Excel with semicolon as delimiter.
     */
    public static CsvFormat excel_semicolon() {
        return builder()
                .newline("\r\n")
                .delimiter(';')
                .skipWhitespaceAroundQuotes(true)
                .allowUnescapedQuotes(true)
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
                .trimUnquotedFields(true)
                .skipWhitespaceAroundQuotes(true)
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
                .doubleQuoteEnabled(false)
                .allowUnescapedQuotes(true)
                .allowUnbalancedQuotes(true)
                .build();
    }

    @Override
    public String toString() {
        return "CsvFormat{" +
                "delimiter='" + printableChar(delimiter) + "'" +
                ", quoteChar='" + printableChar(quoteChar) + "'" +
                ", escapeChar='" + printableChar(escapeChar) + "'" +
                ", newline=\"" + printableString(newline) + "\"" +
                ", alwaysQuote=" + alwaysQuote +
                ", doubleQuoteEnabled=" + doubleQuoteEnabled +
                ", allowUnescapedQuotes=" + allowUnescapedQuotes +
                ", allowUnbalancedQuotes=" + allowUnbalancedQuotes +
                ", trimUnquotedFields=" + trimUnquotedFields +
                ", skipWhitespaceAroundQuotes=" + skipWhitespaceAroundQuotes +
                '}';
    }


    private static String printableChar(char c) {
        return switch (c) {
            case '\t' -> "\\t";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\0' -> "\\0";
            default -> "%c".formatted(c);
        };
    }

    private static String printableString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ++i)
            sb.append(printableChar(s.charAt(i)));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CsvFormat)) return false;
        CsvFormat that = (CsvFormat) o;
        return delimiter == that.delimiter
                && quoteChar == that.quoteChar
                && escapeChar == that.escapeChar
                && newline.equals(that.newline)
                && alwaysQuote == that.alwaysQuote
                && doubleQuoteEnabled == that.doubleQuoteEnabled
                && allowUnescapedQuotes == that.allowUnescapedQuotes
                && allowUnbalancedQuotes == that.allowUnbalancedQuotes
                && trimUnquotedFields == that.trimUnquotedFields
                && skipWhitespaceAroundQuotes == that.skipWhitespaceAroundQuotes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(delimiter, quoteChar, escapeChar, newline, alwaysQuote,
                doubleQuoteEnabled, allowUnescapedQuotes, allowUnbalancedQuotes,
                trimUnquotedFields, skipWhitespaceAroundQuotes);
    }

}