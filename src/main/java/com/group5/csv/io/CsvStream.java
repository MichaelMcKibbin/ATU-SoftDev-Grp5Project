package com.group5.csv.io;

import com.group5.csv.core.Headers;
import com.group5.csv.core.Row;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * High-level streaming API for processing large CSV files without loading
 * them entirely into memory.
 *
 * Responsibilities:
 *   • Wraps a {@link CsvReader} to provide lazy, stream-based iteration
 *   • Exposes Java Stream API for functional operations (filter, map, reduce)
 *   • Manages resource lifecycle through {@link Closeable}
 *   • Processes rows one at a time, maintaining constant memory usage
 *
 * This class is useful for processing large CSV files where loading all rows
 * into memory is impractical. It complements {@link CsvReader#readAll()} which
 * loads all rows at once.
 *
 * Typical usage:
 *
 *   Path file = Paths.get("data.csv");
 *   CsvConfig config = CsvConfig.builder().setHasHeader(true).build();
 *
 *   try (CsvStream stream = CsvStream.fromPath(file, config)) {
 *       stream.stream()
 *           .filter(row -> row.get("amount") != null)
 *           .limit(100)
 *           .forEach(System.out::println);
 *   }
 */
public final class CsvStream implements Closeable {

    private final CsvReader reader;

    /**
     * Creates a new CsvStream wrapping the given CsvReader.
     *
     * @param reader the CSV reader to stream from; must not be null
     * @throws IllegalArgumentException if reader is null
     */
    private CsvStream(CsvReader reader) {
        if (reader == null) throw new IllegalArgumentException("reader must not be null");
        this.reader = reader;
    }

    /**
     * Wraps an existing CsvReader into a CsvStream.
     *
     * @param reader the CSV reader to wrap; must not be null
     * @return a new CsvStream wrapping the reader
     * @throws IllegalArgumentException if reader is null
     */
    public static CsvStream from(CsvReader reader) {
        return new CsvStream(reader);
    }

    /**
     * Opens a CSV file with the given configuration.
     *
     * @param path the path to the CSV file; must not be null
     * @param config the CSV configuration; must not be null
     * @return a new CsvStream for the file
     * @throws Exception if an I/O error occurs or file cannot be read
     */
    public static CsvStream fromPath(Path path, CsvConfig config) throws Exception {
        return new CsvStream(CsvReader.fromPath(path, config));
    }

    /**
     * Opens a CSV file with configuration and explicit headers.
     *
     * @param path the path to the CSV file; must not be null
     * @param config the CSV configuration; must not be null
     * @param headers the explicit headers to use; may be null
     * @return a new CsvStream for the file
     * @throws Exception if an I/O error occurs or file cannot be read
     */
    public static CsvStream fromPath(Path path, CsvConfig config, Headers headers) throws Exception {
        return new CsvStream(CsvReader.fromPath(path, config, headers));
    }

    /**
     * Returns a Java Stream over the rows in this CSV.
     *
     * The stream is lazy-evaluated, processing rows one at a time as they are
     * consumed. This allows efficient processing of large files with minimal
     * memory overhead.
     *
     * Supported operations include:
     *   • filter(Predicate) – select rows matching a condition
     *   • map(Function) – transform rows
     *   • limit(long) – stop after N rows
     *   • forEach(Consumer) – process each row
     *   • count() – count total rows
     *   • collect(Collector) – aggregate rows
     *
     * @return a stream of {@link Row} objects
     */
    public Stream<Row> stream() {
        return StreamSupport.stream(reader.spliterator(), false);
    }

    /**
     * Closes the underlying CsvReader and releases associated resources.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }
}
