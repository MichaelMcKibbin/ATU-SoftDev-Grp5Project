package com.group5.csv.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Immutable configuration class for CSV reading and writing.
 * Used by CsvReader / CsvWriter.
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
    public CsvFormat getFormat()                 { return format; }
    public boolean hasHeader()                   { return hasHeader; }
    public boolean isRequireUniformFieldCount()  { return requireUniformFieldCount; }
    public boolean isSkipEmptyLines()            { return skipEmptyLines; }
    public Charset getCharset()                  { return charset; }
    public boolean isWriteBOM()                  { return writeBOM; }
    public int getReadBufSize()                  { return readBufSize; }

    public static final class Builder {
        private CsvFormat format = CsvFormat.rfc4180();
        private boolean hasHeader = false;
        private boolean requireUniformFieldCount = false;
        private boolean skipEmptyLines = true;
        private Charset charset = StandardCharsets.UTF_8;
        private boolean writeBOM = true;
        private int readBufSize = 8192;

        public Builder setFormat(CsvFormat format) {
            this.format = Objects.requireNonNull(format); return this;
        }
        public Builder setHasHeader(boolean hasHeader) {
            this.hasHeader = hasHeader; return this;
        }
        public Builder setRequireUniformFieldCount(boolean v) {
            this.requireUniformFieldCount = v; return this;
        }
        public Builder setSkipEmptyLines(boolean v) {
            this.skipEmptyLines = v; return this;
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