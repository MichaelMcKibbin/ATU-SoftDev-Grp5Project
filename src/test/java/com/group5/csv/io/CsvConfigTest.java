package com.group5.csv.io;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CsvConfigTest {

    @Test
    void testDefaultValues() {
        CsvConfig config = new CsvConfig.Builder().build();

        assertNotNull(config.getFormat());
        assertFalse(config.hasHeader());
        assertFalse(config.isRequireUniformFieldCount());
        assertTrue(config.isSkipEmptyLines());
        assertEquals(StandardCharsets.UTF_8, config.getCharset());
        assertTrue(config.isWriteBOM());
        assertEquals(8192, config.getReadBufSize());
    }

    @Test
    void testCustomHasHeader() {
        CsvConfig config = new CsvConfig.Builder()
                .setHasHeader(true)
                .build();

        assertTrue(config.hasHeader());
    }

    @Test
    void testSetRequireUniformFieldCount() {
        CsvConfig config = new CsvConfig.Builder()
                .setRequireUniformFieldCount(true)
                .build();

        assertTrue(config.isRequireUniformFieldCount());
    }

    @Test
    void testSetSkipEmptyLines() {
        CsvConfig config = new CsvConfig.Builder()
                .setSkipEmptyLines(false)
                .build();

        assertFalse(config.isSkipEmptyLines());
    }

    @Test
    void testSetCharset() {
        CsvConfig config = new CsvConfig.Builder()
                .setCharset(StandardCharsets.ISO_8859_1)
                .build();

        assertEquals(StandardCharsets.ISO_8859_1, config.getCharset());
    }

    @Test
    void testSetWriteBOM() {
        CsvConfig config = new CsvConfig.Builder()
                .setWriteBOM(false)
                .build();

        assertFalse(config.isWriteBOM());
    }

    @Test
    void testSetReadBufSize() {
        CsvConfig config = new CsvConfig.Builder()
                .setReadBufSize(4096)
                .build();

        assertEquals(4096, config.getReadBufSize());
    }

    @Test
    void testInvalidReadBufSizeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new CsvConfig.Builder().setReadBufSize(0).build()
        );
    }

    @Test
    void builderSetsFormatCorrectly() {
        CsvConfig config = new CsvConfig.Builder().setFormat(CsvFormat.rfc4180()).build();
        assertEquals(config.getFormat(), CsvFormat.rfc4180());
    }

    @Test
    void builderThrowsOnNullFormat() {
        assertThrows(
                NullPointerException.class, () ->
                new CsvConfig.Builder().setFormat(null).build()
        );
    }

    @Test
    void testToStringContainsFields() {
        CsvConfig cfg = new CsvConfig.Builder()
                .setReadBufSize(4096)
                .setCharset(StandardCharsets.UTF_8)
                .setHasHeader(false)
                .setSkipEmptyLines(true)
                .setRequireUniformFieldCount(true)
                .setWriteBOM(true).build();

        String s = cfg.toString();

        assertTrue(s.contains("CsvConfig"));
        assertTrue(s.contains("readBufSize=4096"));
        assertTrue(s.contains("hasHeader=false"));
        assertTrue(s.contains("requireUniformFieldCount=true"));
        assertTrue(s.contains("skipEmptyLines=true"));
        assertTrue(s.contains("writeBOM=true"));
        assertTrue(s.contains("charset=UTF-8"));
    }
}
