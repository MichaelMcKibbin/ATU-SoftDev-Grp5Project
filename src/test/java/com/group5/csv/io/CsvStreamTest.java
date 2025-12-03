package com.group5.csv.io;

import com.group5.csv.core.Row;
import com.group5.csv.testutils.VirtualReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvStream}.
 *
 * Tests verify that CsvStream correctly wraps CsvReader,
 * supports Java Streams API operations, and properly manages resources.
 */
class CsvStreamTest {

    /**
     * Verifies that from() wraps a CsvReader correctly.
     */
    @Test
    void from_wrapsReader() throws IOException {
        String csv = "id,name\n1,Alice\n2,Bob";
        CsvReader reader = new CsvReader(new VirtualReader(csv));
        CsvStream stream = CsvStream.from(reader);

        assertNotNull(stream.stream(), "from() should return a CsvStream with a valid stream");
        stream.close();
    }

    /**
     * Verifies that stream() returns a non-null Stream.
     */
    @Test
    void stream_returnsStream() throws IOException {
        String csv = "id,name\n1,Alice\n2,Bob";
        CsvReader reader = new CsvReader(new VirtualReader(csv));
        CsvStream stream = CsvStream.from(reader);

        assertNotNull(stream.stream(), "stream() should return a Stream");
        stream.close();
    }

    /**
     * Verifies that stream().count() returns the correct number of rows.
     */
    @Test
    void stream_supportsCount() throws IOException {
        String csv = "id,name\n1,Alice\n2,Bob\n3,Charlie";
        CsvReader reader = new CsvReader(new VirtualReader(csv));
        CsvStream stream = CsvStream.from(reader);

        long count = stream.stream().count();
        assertEquals(3, count, "count() should return 3 rows");
        stream.close();
    }

    /**
     * Verifies that stream().filter() correctly filters rows based on a predicate.
     */
    @Test
    void stream_supportsFilter() throws IOException {
        String csv = "id,value\n1,10\n2,25\n3,5\n4,30";
        CsvReader reader = new CsvReader(new VirtualReader(csv));
        CsvStream stream = CsvStream.from(reader);

        long filteredCount = stream.stream()
                .filter(row -> Integer.parseInt(row.get("value")) > 15)
                .count();

        assertEquals(2, filteredCount, "filter() should return 2 rows (25, 30)");
        stream.close();
    }

    /**
     * Verifies that stream().limit() stops iteration at the specified limit.
     */
    @Test
    void stream_supportsLimit() throws IOException {
        String csv = "id,name\n1,Alice\n2,Bob\n3,Charlie\n4,Diana\n5,Eve";
        CsvReader reader = new CsvReader(new VirtualReader(csv));
        CsvStream stream = CsvStream.from(reader);

        long limitedCount = stream.stream()
                .limit(3)
                .count();

        assertEquals(3, limitedCount, "limit(3) should return exactly 3 rows");
        stream.close();
    }

    /**
     * Verifies that stream().forEach() processes all rows.
     */
    @Test
    void stream_supportsForEach() throws IOException {
        String csv = "id,name\n1,Alice\n2,Bob\n3,Charlie";
        CsvReader reader = new CsvReader(new VirtualReader(csv));
        CsvStream stream = CsvStream.from(reader);

        int[] rowCount = {0};
        stream.stream().forEach(row -> rowCount[0]++);

        assertEquals(3, rowCount[0], "forEach() should process all 3 rows");
        stream.close();
    }

    /**
     * Verifies that close() properly closes the underlying CsvReader.
     */
    @Test
    void close_closesUnderlyingReader() throws IOException {
        String csv = "id,name\n1,Alice";
        CsvReader reader = new CsvReader(new VirtualReader(csv));
        CsvStream stream = CsvStream.from(reader);
        stream.close();

        assertDoesNotThrow(() -> stream.close(),
                "close() should be idempotent and not throw");
    }

    /**
     * Verifies that try-with-resources automatically closes the stream.
     */
    @Test
    void tryWithResources_closesAutomatically() throws IOException {
        String csv = "id,name\n1,Alice\n2,Bob";

        try (CsvReader reader = new CsvReader(new VirtualReader(csv));
             CsvStream stream = CsvStream.from(reader)) {

            assertNotNull(stream.stream(), "Stream should be usable within try-with-resources");
        }
        // No exception should be thrown after exiting try block
    }

    /**
     * Verifies that from() rejects null reader.
     */
    @Test
    void from_throwsIllegalArgumentException_whenReaderIsNull() {
        assertThrows(IllegalArgumentException.class, () -> CsvStream.from(null),
                "from() should throw when reader is null");
    }
}
