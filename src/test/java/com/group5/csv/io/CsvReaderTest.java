package com.group5.csv.io;

import com.group5.csv.core.Headers;
import com.group5.csv.core.Row;
import com.group5.csv.exceptions.ParseException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;



class CsvReaderTest {

    /**
     * Tests the default configuration created by the {@code createConfig()} method of the {@link CsvReader} class.
     *
     * This test ensures that the {@link CsvConfig} object returned by the {@code createConfig()} method is initialized
     * with the correct default settings:
     * - The {@code format} is set to {@code CsvFormat.excel()}.
     * - The configuration expects a header row, verified by {@code hasHeader()}.
     * - The configuration does not require uniform field count, checked by {@code isRequireUniformFieldCount()}.
     * - Empty lines in the CSV are skipped, verified by {@code isSkipEmptyLines()}.
     * - The default charset is {@code StandardCharsets.UTF_8}.
     * - The Byte Order Mark (BOM) is written to the output, checked by {@code isWriteBOM()}.
     * - The size of the read buffer is set to 8192 bytes.
     *
     * This method verifies these default settings using assertions to ensure the consistency and reliability
     * of the {@link CsvReader#createConfig()} implementation.
     */
    @Test
    void createConfig_usesExpectedDefaults() {
        CsvReader reader = new CsvReader();

        CsvConfig cfg = reader.createConfig();

        assertEquals(CsvFormat.excel(), cfg.getFormat());
        assertTrue(cfg.hasHeader());
        assertFalse(cfg.isRequireUniformFieldCount());
        assertTrue(cfg.isSkipEmptyLines());
        assertEquals(StandardCharsets.UTF_8, cfg.getCharset());
        assertTrue(cfg.isWriteBOM());
        assertEquals(8192, cfg.getReadBufSize());
    }

    /**
     * Verifies that the {@link CsvConfig.Builder} correctly sets all fields as specified
     * during the builder configuration and that the resulting {@link CsvConfig} object
     * reflects these settings accurately.
     *
     * This test ensures the following:
     * - The selected CSV format is properly set.
     * - Header inclusion is accurately enabled or disabled.
     * - Uniform field count requirement is correctly configured.
     * - Empty line skipping behavior is properly set.
     * - The character set is configured as expected.
     * - BOM writing behavior is accurately set.
     * - The read buffer size is configured to the specified value.
     *
     * Assertions are made on the resulting {@link CsvConfig} instance to confirm each field
     * matches the values configured in the {@link CsvConfig.Builder}.
     */
    @Test
    void builderSetsAllFieldsCorrectly() {
        CsvConfig cfg = new CsvConfig.Builder()
                .setFormat(CsvFormat.excel())
                .setHasHeader(true)
                .setRequireUniformFieldCount(true)
                .setSkipEmptyLines(false)
                .setCharset(StandardCharsets.UTF_8)
                .setWriteBOM(false)
                .setReadBufSize(4096)
                .build();

        assertEquals(CsvFormat.excel(), cfg.getFormat());
        assertTrue(cfg.hasHeader());
        assertTrue(cfg.isRequireUniformFieldCount());
        assertFalse(cfg.isSkipEmptyLines());
        assertEquals(StandardCharsets.UTF_8, cfg.getCharset());
        assertFalse(cfg.isWriteBOM());
        assertEquals(4096, cfg.getReadBufSize());
    }


}
