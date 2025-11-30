package com.group5.csv.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Immutable configuration class controlling CSV parsing and writing behavior.
 * <p>
 * {@code CsvConfig} is used by {@link CsvReader} and {@link CsvWriter} to specify:
 * </p>
 * <ul>
 *     <li>CSV dialect (separator, quoting rules, escape behavior)</li>
 *     <li>Row interpretation rules (headers, field count enforcement)</li>
 *     <li>I/O settings (character set, BOM handling, buffer size)</li>
 * </ul>
 * <p>
 * Instances are immutable and must be created using the nested {@link Builder}.
 * </p>
 */
public final class CsvConfig {

    /** CSV format dialect */
    private final CsvFormat format;

    // General behaviour
    private final boolean hasHeader;
    private final boolean requireUniformFieldCount;
    private final boolean skipEmptyLines;

    // I/O behaviour
    private final Charset charset;
    private final boolean writeBOM;
    private final int readBufSize;

    private CsvConfig(Builder b) {
        this.format = b.format;
        this.hasHeader = b.hasHeader;
        this.requireUniformFieldCount = b.requireUniformFieldCount;
        this.skipEmptyLines = b.skipEmptyLines;
        this.charset = b.charset;
        this.writeBOM = b.writeBOM;
        this.readBufSize = b.readBufSize;
    }

    // Getters

    /**
     * Returns the CSV dialect used for parsing and writing.
     * <p>
     * The format defines delimiter, quoting, escaping, newline handling and
     * other low-level syntax rules.
     * </p>
     *
     * @return the {@link CsvFormat} defining the CSV dialect
     */
    public CsvFormat getFormat()                 { return format; }

    /**
     * Returns whether the reader should treat the first record as a header row.
     * <p>
     * When enabled, {@link CsvReader} will:
     * </p>
     * <ul>
     *   <li>read the first row as column names</li>
     *   <li>expose named lookup via {@code Row.get("ColumnName")}</li>
     *   <li>ignore {@code requireUniformFieldCount}, which is implicitly treated as {@code true}</li>
     * </ul>
     * <p>
     * When disabled, all rows are treated as data rows.
     * </p>
     *
     * @return {@code true} if the first row should be interpreted as headers
     */
    public boolean hasHeader()                   { return hasHeader; }

    /**
     * Returns whether all rows must have the same number of fields.
     * <p><b>Important behavior rules:</b></p>
     * <ul>
     *     <li>If {@code hasHeader == true}, this setting is ignored
     *         because header-based readers must enforce a uniform field count.</li>
     *
     *     <li>If {@code hasHeader == false}:
     *         <ul>
     *             <li>{@code true} → rows are padded or trimmed to match the column count
     *                 of the first row</li>
     *             <li>{@code false} → rows may have any number of fields
     *                 and are returned as-is</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @return {@code true} if strict uniform field count enforcement is requested
     */
    public boolean isRequireUniformFieldCount()  { return requireUniformFieldCount; }

    /**
     * Returns whether empty lines should be skipped while reading.
     * <p><b>Behavior:</b></p>
     * <ul>
     *     <li>{@code true} → blank/empty lines are ignored and do not produce rows</li>
     *     <li>{@code false} → a blank line produces an empty row
     *         (an empty {@code List<String>}), which is different from EOF
     *         where {@code readRow()} returns {@code null}</li>
     * </ul>
     *
     * @return {@code true} if empty lines should be skipped
     */
    public boolean isSkipEmptyLines()            { return skipEmptyLines; }

    /**
     * Returns the character set used for reading and writing CSV data.
     *
     * @return the charset to apply to byte streams
     */
    public Charset getCharset()                  { return charset; }

    /**
     * Returns whether a Unicode Byte Order Mark (BOM) should be written
     * when creating a new CSV output stream.
     * <p>
     * Has no effect on reading. Writing a BOM is useful for Excel compatibility.
     * </p>
     *
     * @return {@code true} if BOM should be written on output
     */
    public boolean isWriteBOM()                  { return writeBOM; }

    /**
     * Returns the size of the internal read buffer in bytes.
     * <p>
     * This impacts I/O performance when reading large CSV files.
     * </p>
     *
     * @return the buffer size in bytes
     */
    public int getReadBufSize()                  { return readBufSize; }


    /**
     * Mutable builder used to construct an immutable {@link CsvConfig} instance.
     * <p>
     * All options have safe defaults:
     * </p>
     * <ul>
     *   <li>RFC 4180 format</li>
     *   <li>No header row</li>
     *   <li>Uniform field count disabled</li>
     *   <li>Empty lines skipped</li>
     *   <li>UTF-8 charset</li>
     *   <li>BOM enabled</li>
     *   <li>8 KB buffer</li>
     * </ul>
     * <p>
     * Each setter overrides a single option and returns the builder itself.
     * </p>
     */
    public static final class Builder {
        private CsvFormat format = CsvFormat.rfc4180();
        private boolean hasHeader = false;
        private boolean requireUniformFieldCount = false;
        private boolean skipEmptyLines = true;
        private Charset charset = StandardCharsets.UTF_8;
        private boolean writeBOM = true;
        private int readBufSize = 8192;

        /**
         * Sets the CSV syntax dialect defining separator, quoting and escaping rules.
         *
         * @param format the {@link CsvFormat} to use
         * @return this builder
         */
        public Builder setFormat(CsvFormat format) {
            this.format = Objects.requireNonNull(format); return this;
        }

        /**
         * Enables or disables interpretation of the first row as a header row.
         *
         * @param hasHeader {@code true} to treat the first row as headers
         * @return this builder
         */
        public Builder setHasHeader(boolean hasHeader) {
            this.hasHeader = hasHeader; return this;
        }

        /**
         * Enables or disables enforcement of a uniform field count across all rows.
         * <p>
         * If {@code hasHeader == true}, this setting is ignored because readers
         * automatically require uniform field counts after reading the header row.
         * </p>
         *
         * @param v {@code true} for strict column count enforcement
         * @return this builder
         */
        public Builder setRequireUniformFieldCount(boolean v) {
            this.requireUniformFieldCount = v; return this;
        }

        /**
         * Sets whether blank lines should be skipped or returned as empty rows.
         *
         * @param v {@code true} to skip empty lines, {@code false} to return empty rows
         * @return this builder
         */
        public Builder setSkipEmptyLines(boolean v) {
            this.skipEmptyLines = v; return this;
        }

        /**
         * Sets the character encoding used when reading from or writing to byte streams.
         *
         * @param charset the charset to use
         * @return this builder
         */
        public Builder setCharset(Charset charset) {
            this.charset = Objects.requireNonNull(charset); return this;
        }

        /**
         * Sets whether a BOM should be written to output streams.
         *
         * @param writeBOM {@code true} to write a BOM when outputting CSV
         * @return this builder
         */
        public Builder setWriteBOM(boolean writeBOM) {
            this.writeBOM = writeBOM; return this;
        }

        /**
         * Sets the size of the internal buffer used for reading.
         *
         * @param readBufSize buffer size in bytes; must be &gt; 0
         * @return this builder
         * @throws IllegalArgumentException if {@code readBufSize <= 0}
         */
        public Builder setReadBufSize(int readBufSize) {
            this.readBufSize = readBufSize; return this;
        }

        /**
         * Builds and returns a new immutable {@link CsvConfig} instance.
         *
         * @return the constructed configuration object
         * @throws IllegalArgumentException if invalid settings are provided
         */
        public CsvConfig build() {
            if (readBufSize <= 0)
                throw new IllegalArgumentException("readBufSize must be > 0");
            return new CsvConfig(this);
        }
    }

    @Override
    public String toString() {
        return "CsvConfig{" +
                "hasHeader=" + hasHeader +
                ", requireUniformFieldCount=" + requireUniformFieldCount +
                ", skipEmptyLines=" + skipEmptyLines +
                ", charset=" + charset +
                ", writeBOM=" + writeBOM +
                ", readBufSize=" + readBufSize +
                ", format=" + format +
                '}';
    }
}