package com.group5.csv.io;

import java.io.*;
import java.nio.charset.*;

/**
 *      InputStreamDetector
 * ----------------------------------------------------------------------------
 * Handles BOM sequence for UTF encodings and creates corresponding input stream.
 * If CsvConfig is set to non-UTF encoding, BOM detection is skipped
 *
 * Encoding	    BOM bytes (hex) Notes
 * UTF-8	    EF BB BF	    Common in Windows-generated files (e.g. Excel CSVs)
 * UTF-16 LE	FF FE	        Little-endian UTF-16
 * UTF-16 BE	FE FF	        Big-endian UTF-16
 * UTF-32 LE	FF FE 00 00	    Rare
 * UTF-32 BE	00 00 FE FF	    Rare
 */

public class InputStreamDetector {
    public static class Result {
        public final InputStream stream;
        public final Charset charset;

        public Result(InputStream stream, Charset charset) {
            this.stream = stream;
            this.charset = charset;
        }
    }

    /**
     * Detect BOM if charset is UTF-8 (or UTF-16/32) and return InputStream after BOM.
     * If charset is explicitly specified and not BOM-aware, skip detection.
     */
    public static Result detect(File file, Charset charset) throws IOException
    {
        if (charset != StandardCharsets.UTF_8 &&
                charset != StandardCharsets.UTF_16BE &&
                charset != StandardCharsets.UTF_16LE) {
            // User requested a non-BOM-aware charset → skip detection
            return new Result(new FileInputStream(file), charset);
        }

        FileInputStream fis = new FileInputStream(file);
        PushbackInputStream pb = new PushbackInputStream(fis, 4);

        byte[] bom = new byte[4];
        int n = pb.read(bom, 0, bom.length);

        Charset detectedCharset;
        int unread;

        if (n >= 4 && bom[0] == (byte)0x00 && bom[1] == (byte)0x00 &&
                bom[2] == (byte)0xFE && bom[3] == (byte)0xFF) {
            detectedCharset = Charset.forName("UTF-32BE");
            unread = n - 4;
        } else if (n >= 4 && bom[0] == (byte)0xFF && bom[1] == (byte)0xFE &&
                bom[2] == (byte)0x00 && bom[3] == (byte)0x00) {
            detectedCharset = Charset.forName("UTF-32LE");
            unread = n - 4;
        } else if (n >= 2 && bom[0] == (byte)0xFE && bom[1] == (byte)0xFF) {
            detectedCharset = StandardCharsets.UTF_16BE;
            unread = n - 2;
        } else if (n >= 2 && bom[0] == (byte)0xFF && bom[1] == (byte)0xFE) {
            detectedCharset = StandardCharsets.UTF_16LE;
            unread = n - 2;
        } else if (n >= 3 && bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF) {
            detectedCharset = StandardCharsets.UTF_8;
            unread = n - 3;
        } else {
            detectedCharset = charset; // fallback to requested charset
            unread = n;
        }

        if (unread > 0) {
            pb.unread(bom, n - unread, unread);
        }

        return new Result(pb, detectedCharset);
    }
}
