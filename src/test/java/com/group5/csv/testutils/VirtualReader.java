package com.group5.csv.testutils;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * Simple Reader implementation that wraps a String.
 * Useful for unit tests where predictable input is required.
 */
public class VirtualReader extends Reader {

    private final String data;
    private int pos = 0;
    private boolean closed = false;

    public VirtualReader(String data) {
        this.data = Objects.requireNonNull(data, "data must not be null");
    }

    @Override
    public int read() throws IOException {
        ensureOpen();
        if (pos >= data.length()) {
            return -1; // EOF
        }
        return data.charAt(pos++);
    }

    @Override
    public int read(char[] buffer, int off, int len) throws IOException {
        ensureOpen();
        Objects.checkFromIndexSize(off, len, buffer.length);

        if (pos >= data.length()) {
            return -1; // EOF
        }

        int remaining = data.length() - pos;
        int toCopy = Math.min(len, remaining);

        data.getChars(pos, pos + toCopy, buffer, off);
        pos += toCopy;
        return toCopy;
    }

    @Override
    public void close() {
        closed = true;
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Reader already closed");
        }
    }
}