package com.group5.csv.io;

import com.group5.csv.core.Field;
import com.group5.csv.core.FieldType;
import com.group5.csv.schema.DecimalSpec; // used indirectly via FieldType.format
import com.group5.csv.schema.DateTimeSpec; // likewise, via FieldType

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CsvWriter implements AutoCloseable {
    private final CsvPrinter printer;
    private final List<Field> fields;   // ordered by output
    private final List<String> headers; // names matching fields
    private boolean wroteHeader = false;

    public CsvWriter(Writer out, List<Field> fields, List<String> headers, CsvFormat format) {
        this.printer = new CsvPrinter(out, format);
        this.fields = fields;
        this.headers = headers;
    }

    /** Writes header once only. */
    public void writeHeaderIfNeeded() throws IOException {
        if (!wroteHeader) {
            printer.printRow(headers);
            wroteHeader = true;
        }
    }

    /** Write a row from a name->value map (round-trip safe). */
    public void writeRow(Map<String, Object> nameToValue) throws IOException {
        writeHeaderIfNeeded();
        List<String> outRow = new ArrayList<>(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            Object value = nameToValue.get(headers.get(i)); // header names align
            outRow.add(formatField(value, f));
        }
        printer.printRow(outRow);
    }

    /** Write from a typed Row (adapt to your Row API). */
    public void writeRow(RowLike row) throws IOException {
        writeHeaderIfNeeded();
        List<String> outRow = new ArrayList<>(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            Object value = row.get(i); // or row.get(headers.get(i))
            outRow.add(formatField(value, f));
        }
        printer.printRow(outRow);
    }

    private String formatField(Object value, Field field) {
        FieldType t = field.type();
        // Delegate to FieldType.format – this preserves round-trip with your parser
        return t.format(value, field);
    }

    public void flush() throws IOException { printer.flush(); }
    @Override public void close() throws IOException { printer.close(); }

    /** Replace with your real Row type or Map-based writing only. */
    public interface RowLike {
        Object get(int index);
    }
}
