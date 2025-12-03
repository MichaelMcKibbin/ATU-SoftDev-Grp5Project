package com.group5.csv.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class InputStreamDetectorTest {

    @TempDir
    Path tempDir;

    // ---- Helper to create temp file with raw bytes ----
    private File createTempFile(byte[] bytes) throws IOException {
        File temp = tempDir.resolve("bomtest.csv").toFile();
        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write(bytes);
        }
        return temp;
    }

    // ---- Helper to read full stream content ----
    private byte[] readAll(InputStream in) throws IOException {
        return in.readAllBytes();
    }

    // --- Util: concatenate byte arrays ---
    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    @Test
    void testDetectUtf8Bom() throws Exception {
        byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] content = "abc".getBytes(StandardCharsets.UTF_8);
        File f = createTempFile(concat(bom, content));

        var r = InputStreamDetector.detect(f, StandardCharsets.UTF_8);

        try (InputStream in = r.stream) {
            assertEquals(StandardCharsets.UTF_8, r.charset);
            assertArrayEquals(content, readAll(in)); // BOM removed
        }
    }

    @Test
    void testDetectUtf16LEBom() throws Exception {
        byte[] bom = {(byte)0xFF, (byte)0xFE};
        byte[] content = "de".getBytes(StandardCharsets.UTF_16LE);
        File f = createTempFile(concat(bom, content));

        var r = InputStreamDetector.detect(f, StandardCharsets.UTF_16LE);

        try (InputStream in = r.stream) {
            assertEquals(StandardCharsets.UTF_16LE, r.charset);
            assertArrayEquals(content, readAll(r.stream)); // BOM removed
        }
    }

    @Test
    void testDetectUtf16BEBom() throws Exception {
        byte[] bom = {(byte)0xFE, (byte)0xFF};
        byte[] content = "fghi".getBytes(StandardCharsets.UTF_16BE);
        File f = createTempFile(concat(bom, content));

        var r = InputStreamDetector.detect(f, StandardCharsets.UTF_16BE);

        try (InputStream in = r.stream) {
            assertEquals(StandardCharsets.UTF_16BE, r.charset);
            assertArrayEquals(content, readAll(r.stream)); // BOM removed
        }
    }

    @Test
    void testDetectUtf32LEBom() throws Exception {
        byte[] bom = {(byte)0xFF, (byte)0xFE, 0x00, 0x00};
        byte[] content = "j".getBytes("UTF-32LE");
        File f = createTempFile(concat(bom, content));

        var r = InputStreamDetector.detect(f, StandardCharsets.UTF_8); // request ANY UTF to trigger detection

        try (InputStream in = r.stream) {
            assertEquals(Charset.forName("UTF-32LE"), r.charset);
            assertArrayEquals(content, readAll(r.stream)); // BOM removed
        }
    }

    @Test
    void testDetectUtf32BEBom() throws Exception {
        byte[] bom = {0x00, 0x00, (byte)0xFE, (byte)0xFF};
        byte[] content = "klm".getBytes("UTF-32BE");
        File f = createTempFile(concat(bom, content));

        var r = InputStreamDetector.detect(f, StandardCharsets.UTF_8);

        try (InputStream in = r.stream) {
            assertEquals(Charset.forName("UTF-32BE"), r.charset);
            assertArrayEquals(content, readAll(r.stream));
        }
    }

    @Test
    void testNoBomFallbackToRequestedUtf() throws Exception {
        byte[] content = "op".getBytes(StandardCharsets.UTF_8);
        File f = createTempFile(content);

        var r = InputStreamDetector.detect(f, StandardCharsets.UTF_8);

        try (InputStream in = r.stream) {
            assertEquals(StandardCharsets.UTF_8, r.charset); // same as requested
            assertArrayEquals(content, readAll(r.stream));    // no bytes skipped
        }
    }

    @Test
    void testSkipDetectionForNonUtfCharset() throws Exception {
        byte[] bomUtf8 = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] content = "qrstuvw".getBytes(StandardCharsets.ISO_8859_1);
        File f = createTempFile(concat(bomUtf8, content));

        // Expect: No BOM detection, full file including BOM is returned
        var r = InputStreamDetector.detect(f, StandardCharsets.ISO_8859_1);

        try (InputStream in = r.stream) {
            assertEquals(StandardCharsets.ISO_8859_1, r.charset); // unchanged
            assertArrayEquals(concat(bomUtf8, content), readAll(r.stream));
        }
    }
}
