package com.group5.csv.io;

import java.util.Objects;

/**
 * Immutable description of a CSV dialect (delimiter, quoting, escaping, newline and related rules).
 * <p>Use presets (e.g. {@link #rfc4180()}, {@link #excel()}, {@link #tsv()}) or {@link Builder}
 * to create a custom format. This class is immutable and thread-safe.
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

    /**
     * Creates a copy of this format using the provided delimiter.
     *
     * @param d the new delimiter character
     * @return a new {@code CsvFormat} instance with {@code delimiter==d}
     */
    public CsvFormat withDelimiter(char d) {
        return new CsvFormat(d, quoteChar, newline, alwaysQuote,
                escapeChar, doubleQuoteEnabled, allowUnbalancedQuotes,
                allowUnescapedQuotes, trimUnquotedFields,
                skipWhitespaceAroundQuotes);
    }

    /**
     * Creates a copy of this format using the provided quote character.
     *
     * @param q the new quote character
     * @return a new {@code CsvFormat} instance with {@code quoteChar==q}
     */
    public CsvFormat withQuoteChar(char q) {
        return new CsvFormat(delimiter, q, newline, alwaysQuote,
                escapeChar, doubleQuoteEnabled, allowUnbalancedQuotes,
                allowUnescapedQuotes, trimUnquotedFields,
                skipWhitespaceAroundQuotes);
    }

    /**
     * Creates a copy of this format using the provided newline sequence.
     *
     * @param nl the newline string (e.g. "\n" or "\r\n")
     * @return a new {@code CsvFormat} with the given newline
     */
    public CsvFormat withNewline(String nl) {
        return new CsvFormat(delimiter, quoteChar, nl, alwaysQuote,
                escapeChar, doubleQuoteEnabled, allowUnbalancedQuotes,
                allowUnescapedQuotes, trimUnquotedFields,
                skipWhitespaceAroundQuotes);
    }

    /**
     * Creates a copy of this format setting whether all fields are always quoted.
     *
     * @param v {@code true} to always quote fields
     * @return a new {@code CsvFormat} with {@code alwaysQuote==v}
     */
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


    /**
     * Returns a new {@link Builder} for constructing custom {@code CsvFormat} instances.
     *
     * @return a fresh builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link CsvFormat}.
     * <p>Use the fluent setters to override defaults, then call {@link #build()}.
     */
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

        /**
         * Set the field delimiter (default ',').
         *
         * @param d delimiter character
         * @return this builder
         */
        public Builder delimiter(char d) {
            this.delimiter = d;
            return this;
        }

        /**
         * Set the quote character (default '\"'). Use {@link #NO_QUOTE} to disable quoting.
         *
         * @param q quote character
         * @return this builder
         */
        public Builder quoteChar(char q) {
            this.quoteChar = q;
            return this;
        }

        /**
         * Set the newline sequence to use when writing (default "\n").
         *
         * @param nl newline string
         * @return this builder
         */
        public Builder newline(String nl) {
            this.newline = nl;
            return this;
        }

        /**
         * When true, the writer will quote every field.
         *
         * @param v true to always quote
         * @return this builder
         */
        public Builder alwaysQuote(boolean v) {
            this.alwaysQuote = v;
            return this;
        }

        /**
         * Set the escape character (use {@link #NO_ESCAPE} to disable escaping).
         *
         * @param c escape character
         * @return this builder
         */
        public Builder escapeChar(char c) {
            this.escapeChar = c;
            return this;
        }

        /**
         * Enable or disable double-quote escaping inside quoted fields ("" -> ").
         *
         * @param v true to enable double-quote handling
         * @return this builder
         */
        public Builder doubleQuoteEnabled(boolean v) {
            this.doubleQuoteEnabled = v;
            return this;
        }

        /**
         * When true, parser accepts quotes inside unquoted fields (lenient).
         *
         * @param v allow unescaped quotes in unquoted fields
         * @return this builder
         */
        public Builder allowUnescapedQuotes(boolean v) {
            this.allowUnescapedQuotes = v;
            return this;
        }

        /**
         * When true, parser tolerates a missing closing quote (lenient).
         *
         * @param v allow unbalanced quotes
         * @return this builder
         */
        public Builder allowUnbalancedQuotes(boolean v) {
            this.allowUnbalancedQuotes = v;
            return this;
        }

        /**
         * Trim whitespace around unquoted fields when parsing.
         *
         * @param v true to trim unquoted fields
         * @return this builder
         */
        public Builder trimUnquotedFields(boolean v) {
            this.trimUnquotedFields = v;
            return this;
        }

        /**
         * Ignore whitespace between delimiters and surrounding quotes (Excel style).
         *
         * @param v true to skip whitespace around quotes
         * @return this builder
         */
        public Builder skipWhitespaceAroundQuotes(boolean v) {
            this.skipWhitespaceAroundQuotes = v;
            return this;
        }

        /**
         * Build the immutable {@link CsvFormat}. Validation errors (e.g. empty newline)
         * are reported as {@link IllegalArgumentException}.
         *
         * @return the constructed {@code CsvFormat}
         */
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
     * Returns an RFC-4180-like default format (LF newline, comma delimiter, standard quoting).
     *
     * @return a standard RFC-4180 style format
     */
    public static CsvFormat rfc4180() {
        return builder().build();
    }

    /**
     * Returns an Excel-like format (CRLF newline, permissive whitespace/quote handling).
     *
     * @return an Excel-friendly {@code CsvFormat}
     */
    public static CsvFormat excel() {
        return builder()
                .newline("\r\n")
                .skipWhitespaceAroundQuotes(true)
                .allowUnescapedQuotes(true)
                .build();
    }

    /**
     * Returns an Excel variant using semicolon as delimiter.
     *
     * @return an Excel-style {@code CsvFormat} with ';' delimiter
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
     * Returns a lenient JSON-style CSV preset (backslash escaping, tolerant quotes).
     *
     * @return a permissive {@code CsvFormat} suitable for JSON-like CSV
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
     * Returns a TSV preset (tab delimiter, quoting/escaping disabled).
     *
     * @return a {@code CsvFormat} configured for TSV
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
        if (!(o instanceof CsvFormat that)) return false;
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