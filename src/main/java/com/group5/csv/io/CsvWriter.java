package com.group5.csv.io;

import com.group5.csv.core.Headers;
import com.group5.csv.core.Row;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * High-level CSV writer that wraps {@link CsvPrinter} and exposes
 * a convenient API for writing rows of values.
 *
 * Responsibilities:
 *   • Owns the underlying {@link Writer} and {@link CsvPrinter}
 *   • Applies the dialect defined in {@link CsvConfig#getFormat()}
 *   • Optionally writes a header row before data
 *   • Provides simple methods for writing single rows and collections
 *
 * This class is the counterpart to {@link CsvReader}: where CsvReader
 * turns CSV text → rows of values; CsvWriter takes rows of values → CSV text.
 *
 * Typical usage:
 *
 *   CsvConfig config = CsvConfig.builder()
 *       .format(CsvFormat.rfc4180())
 *       .hasHeader(true)
 *       .build();
 *
 *   try (CsvWriter writer = CsvWriter.toPath(Path.of("out.csv"), config)) {
 *       writer.writeHeader(List.of("id", "name", "age"));
 *       writer.writeRow(List.of("1", "Alice", "30"));
 *       writer.writeRow(List.of("2", "Bob", "40"));
 *   }
 */
public final class CsvWriter implements Closeable, Flushable {

    private final CsvConfig config;
    private final Writer out;
    private final CsvPrinter printer;

    /** Whether a header row has already been written. */
    private boolean headerWritten = false;

    /**
     * Primary constructor: wraps the given {@link Writer} and uses the
     * dialect from {@link CsvConfig#getFormat()}.
     *
     * @param out    destination text stream (will be wrapped in BufferedWriter if necessary)
     * @param config CSV configuration / dialect
     */
    public CsvWriter(Writer out, CsvConfig config) {
        if (out == null) throw new IllegalArgumentException("out must not be null");
        if (config == null) throw new IllegalArgumentException("config must not be null");

        this.config = config;

        // Ensure we have buffering; avoid double-wrapping if caller already supplied one.
        Writer effectiveOut = (out instanceof BufferedWriter) ? out : new BufferedWriter(out);
        this.out = effectiveOut;

        // CsvPrinter is the low-level, dialect-aware writer.
        this.printer = new CsvPrinter(effectiveOut, config.getFormat());
    }

    /**
     * Convenience constructor: create a writer from a byte stream using
     * the charset defined in the config.
     */
    public CsvWriter(OutputStream out, CsvConfig config) {
        this(new OutputStreamWriter(
                Objects.requireNonNull(out, "out must not be null"),
                Objects.requireNonNull(config, "config must not be null").getCharset()
        ), config);
    }

    /**
     * Convenience factory: open a file for writing using the charset and
     * dialect from the supplied config.
     *
     * This mirrors the {@code CsvReader.fromPath(...)} style and is handy
     * for demo / main.
     */
    public static CsvWriter toPath(Path path, CsvConfig config) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(config, "config must not be null");

        Charset cs = config.getCharset();
        Writer fileWriter = Files.newBufferedWriter(path, cs);
        return new CsvWriter(fileWriter, config);
    }

    public CsvConfig getConfig() {
        return config;
    }

    /**
     * Writes a header row once, if your CSV has headers.
     * <p>
     * This does not try to infer anything from {@code CsvConfig.hasHeader()}:
     * it simply writes the given cells as the first row and remembers that
     * a header was written. Callers decide whether/when to emit the header.
     *
     * @param headerCells column names in display order
     * @throws IOException              if the underlying writer fails
     * @throws IllegalStateException    if a header has already been written
     * @throws IllegalArgumentException if headerCells is null
     */
    public void writeHeader(List<String> headerCells) throws IOException {
        if (headerCells == null) {
            throw new IllegalArgumentException("headerCells must not be null");
        }
        if (headerWritten) {
            throw new IllegalStateException("Header row has already been written");
        }
        printer.printRow(headerCells);
        headerWritten = true;
    }

    /**
     * Writes a single data row to the output.
     *
     * Values are converted to strings via {@code String.valueOf(...)} so you
     * can safely pass numbers, enums, etc. If you want full control over
     * formatting (dates, decimals), pre-format to String before calling.
     *
     * @param values row of values in column order
     * @throws IOException              if the underlying writer fails
     * @throws IllegalArgumentException if values is null
     */
    public void writeRow(List<?> values) throws IOException {
        if (values == null) {
            throw new IllegalArgumentException("values must not be null");
        }

        // Convert all values to String for CsvPrinter
        // (CsvPrinter is purely about CSV syntax, not typing).
        int size = values.size();
        newRowBuffer.clear();
        for (int i = 0; i < size; i++) {
            Object v = values.get(i);
            newRowBuffer.add(v == null ? "" : String.valueOf(v));
        }

        printer.printRow(newRowBuffer);
    }

    /**
     * Writes a single {@link Row} using its {@link Headers} to determine
     * the column order and raw string cell values.
     * <p>
     * Assumptions about the Row/Headers API:
     * <ul>
     *   <li>{@code row.getHeaders()} returns the headers object for this row.</li>
     *   <li>{@code headers.size()} is the number of columns.</li>
     *   <li>{@code row.get(int index)} returns the raw cell text for that column index.</li>
     * </ul>
     * If your actual API differs (e.g. {@code headers.getName(i)} +
     * {@code row.getString(name)}), adapt inside this method.
     */
    public void writeRow(Row row) throws IOException {
        if (row == null) {
            throw new IllegalArgumentException("row must not be null");
        }

        Headers headers = row.getHeaders();   // adjust method name if needed
        int columnCount = headers.size();     // adjust if you use a different API

        newRowBuffer.clear();
        for (int i = 0; i < columnCount; i++) {
            // If your Row API is name-based, you might instead do:
            //   String colName = headers.getName(i);
            //   String value = row.getString(colName);
            String value = row.get(i);        // adjust to getRaw(i) / getString(i) etc.
            newRowBuffer.add(value == null ? "" : value);
        }

        printer.printRow(newRowBuffer);
    }

    /**
     * Writes all rows from the provided iterable to the output.
     *
     * @param rows an iterable collection of {@link Row} objects to be written
     * @throws IOException if an I/O error occurs during writing
     */
    public void writeAllRows(Iterable<Row> rows) throws IOException {
        for (Row row : rows) {
            writeRow(row);
        }
    }


    // Small reusable buffer to avoid allocating a new List every call.
    // If you prefer immutability over micro-alloc optimization, you can
    // remove this and build a new ArrayList inside writeRow.
    private final java.util.ArrayList<String> newRowBuffer = new java.util.ArrayList<>();

    /**
     * Writes all rows from the given iterable.
     *
     * @param rows iterable of rows, each a {@code List<?>} of cell values
     * @throws IOException if any write fails
     */
    public void writeAll(Iterable<? extends List<?>> rows) throws IOException {
        if (rows == null) {
            throw new IllegalArgumentException("rows must not be null");
        }
        for (List<?> row : rows) {
            writeRow(row);
        }
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        // Let try-with-resources close the underlying stream.
        // CsvPrinter writes directly to 'out', so closing 'out' is enough.
        try {
            flush();
        } finally {
            out.close();
        }
    }
}
