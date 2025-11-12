package com.group5.csv.io;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * CsvPrinter is the low-level writer responsible for producing valid CSV text.
 *
 * It receives rows of pre-formatted String values (already converted by
 * higher-level components like CsvWriter or FieldType) and writes them to
 * a text stream using the formatting rules defined in {@link CsvFormat}.
 *
 * Responsibilities:
 *  • Join columns using the configured delimiter (e.g. ',')
 *  • Quote and escape fields that contain special characters
 *  • Write newlines at the end of each record
 *
 * This class does NOT know about schemas, field types, or validation.
 * Its only job is to output correct, RFC-4180-compliant CSV syntax.
 *
 * Typical usage:
 *   CsvPrinter printer = new CsvPrinter(writer, CsvFormat.rfc4180());
 *   printer.printRow(List.of("Jimmy", "40", "Hello, world"));
 *   printer.close();
 */
public final class CsvPrinter {
    private final Writer out;
    private final CsvFormat fmt;

    /**
     * Creates a new CsvPrinter that writes to the given Writer
     * using the specified CsvFormat.
     *
     * @param out the underlying Writer (e.g., FileWriter or StringWriter)
     * @param fmt the CsvFormat specifying delimiter, quoting, etc.
     */
    public CsvPrinter(Writer out, CsvFormat fmt) {
        this.out = out;
        this.fmt = fmt;
    }

    /**
     * Writes one row of CSV output.
     * Each element of the list corresponds to a single cell value.
     *
     * @param cells ordered list of values for one row
     */
    public void printRow(List<String> cells) throws IOException {
        for (int i = 0; i < cells.size(); i++) {
            // Write the delimiter before every cell except the first
            if (i > 0) out.write(fmt.delimiter);
            writeCell(cells.get(i));
        }
        // End the row with the configured newline sequence
        out.write(fmt.newline);
    }

    /**
     * Writes a single cell, quoting and escaping if necessary.
     *
     * Rules (RFC-4180 style):
     *  - If the field contains a delimiter, newline, or quote,
     *    it must be enclosed in quotes.
     *  - Any quotes inside a quoted field are escaped by doubling them.
     *  - If alwaysQuote = true, all fields are quoted regardless of content.
     */
    private void writeCell(String v) throws IOException {
        if (v == null) v = "";

// ---------------------------------------------------------------------
// Determine whether this cell needs to be wrapped in quotes.
//
// In CSV, quoting is required in several situations to preserve data integrity.
// The main cases are defined by RFC-4180 (the de facto CSV standard):
//
//   1. If the field contains a delimiter (e.g., ',' or ';')
//      → Quoting prevents it from being mistaken as a column separator.
//
//   2. If the field contains a newline (\n) or carriage return (\r)
//      → Quoting ensures multi-line text stays inside one cell.
//
//   3. If the field itself contains a quote character (e.g., "Hello "World"")
//      → The field must be quoted, and internal quotes doubled.
//
//   4. If the field begins or ends with whitespace
//      → Quoting preserves the spaces; otherwise they’d be trimmed by some readers.
//
//   5. If the configuration (CsvFormat) says "alwaysQuote = true"
//      → Every field is quoted, even if not strictly required.
//        This is often used for compatibility with older systems or Excel.
//
// The result of these checks is stored in 'mustQuote'.
// If true, the cell will be enclosed in quotes and any embedded quotes escaped.
//
        boolean mustQuote = fmt.alwaysQuote
                || v.indexOf(fmt.delimiter) >= 0   // (1) Contains a delimiter
                || v.indexOf('\n') >= 0            // (2) Contains a linefeed
                || v.indexOf('\r') >= 0            // (2) Contains a carriage return
                || v.indexOf(fmt.quoteChar) >= 0   // (3) Contains a quote character
                || v.startsWith(" ")               // (4) Starts with a space
                || v.endsWith(" ");                // (4) Ends with a space
// ---------------------------------------------------------------------
// If none of these conditions apply, the value is "safe" to write as-is.
// Example safe values: "123", "Bob", "2025-11-11"
// Example unsafe values: "Bob, Inc.", "He said "Hi"", "  leading space"
// Those will be quoted automatically.

        if (!mustQuote) {
            // Fast path: safe value, no quotes needed
            out.write(v);
            return;
        }

        // Quote the entire field and escape any internal quotes by doubling them
        out.write(fmt.quoteChar);
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (c == fmt.quoteChar) {
                // Example: value = He said "Hi"
                // Output = "He said ""Hi"""
                out.write(fmt.quoteChar);
                out.write(fmt.quoteChar);
            } else {
                out.write(c);
            }
        }
        out.write(fmt.quoteChar);
    }

    /** Flush any buffered output down to the underlying Writer. */
    public void flush() throws IOException { out.flush(); }

    /** Close the underlying Writer when done writing all rows. */
    public void close() throws IOException { out.close(); }
}
