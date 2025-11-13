package com.group5.csv.io;

/**
 *      CsvConfig
 * ----------------------------------------------------------------------------
 * Immutable configuration class for CSV reading and writing.
 * Used by classes: CsvReader, CSVWriter
 *
 * Example of usage: CsvConfig config = new CsvConfig.Builder()
 *             .setFormat(CsvFormat.rfc4180())
 *             .setHasHeader(true)
 *             .setReadBufSize(80192)
 *             .setSkipEmptyLines(false).build();
 */

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class CsvConfig {

    /** CSV format dialect */
    private final CsvFormat format;

    // --- General behaviour ---
    /** Effects both CsvReader and CsvWriter. */
    private final boolean hasHeader;

    /** Effects CsvReader. Throws Exception if number of fields differs from first row */
    private final boolean requireUniformFieldCount;

    /** Effects CsvReader. Add empty lines as empty rows or not */
    private final boolean skipEmptyLines;

    // --- I/O behavior ---
    /** charset */
    private final Charset charset;

    /** write bom sequence (for UTF charset only) */
    private final boolean writeBOM;

    /** size of read buffer */
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

    // --- Getters ---
    public boolean hasHeader()               { return hasHeader; }
    public boolean isRequireUniformFieldCount() { return requireUniformFieldCount; }
    public boolean isSkipEmptyLines()        { return skipEmptyLines; }
    public Charset getCharset()              { return charset; }
    public boolean isWriteBOM()              { return writeBOM; }
    public int getReadBufSize()              { return readBufSize; }
    public boolean isHasHeader()             { return hasHeader; }
    public CsvFormat getFormat()             { return format; }

    // --- Builder ---
    public static final class Builder {
        private boolean hasHeader = false;
        private boolean requireUniformFieldCount = false;
        private boolean skipEmptyLines = true;
        private Charset charset = StandardCharsets.UTF_8;
        private boolean writeBOM = true;
        private int readBufSize = 8192;
        private CsvFormat format = CsvFormat.rfc4180();

        // Default constructor
        public Builder() {}

        public Builder setHasHeader(boolean hasHeader) {
            this.hasHeader = hasHeader; return this;
        }
        public Builder setRequireUniformFieldCount(boolean requireUniformFieldCount) {
            this.requireUniformFieldCount = requireUniformFieldCount; return this;
        }
        public Builder setSkipEmptyLines(boolean skipEmptyLines) {
            this.skipEmptyLines = skipEmptyLines; return this;
        }
        public Builder setCharset(Charset charset) {
            this.charset = Objects.requireNonNull(charset); return this;
        }
        public Builder setWriteBOM(boolean writeBOM) {
            this.writeBOM = writeBOM; return this;
        }
        public Builder setReadBufSize(int readBufSize) {
            this.readBufSize = readBufSize; return this;
        }
        public Builder setFormat(CsvFormat format) {
            this.format = format; return this;
        }

        public CsvConfig build() {
            return new CsvConfig(this);
        }
    }

    // --- Utility ---
    @Override
    public String toString() {
        return "CsvConfig{" +
                ", hasHeader=" + hasHeader +
                ", requireUniformFieldCount=" + requireUniformFieldCount +
                ", skipEmptyLines=" + skipEmptyLines +
                ", charset=" + charset +
                ", writeBOM=" + writeBOM +
                ", readBufSize=" + readBufSize +
                ", format=" + format.toString() +
                '}';
    }
}