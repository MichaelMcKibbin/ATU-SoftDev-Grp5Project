package com.group5.csv.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mutable helper for constructing {@link Row} instances.
 * <p>
 * Typical usage (e.g. in CsvReader):
 * <pre>{@code
 * RowBuilder builder = new RowBuilder(headers);
 * for (String cell : parsedCells) {
 *     builder.add(cell);
 * }
 * Row row = builder.build();
 * }</pre>
 *
 * The builder enforces that the number of values matches the number of
 * columns in the associated {@link Headers} at build time.
 */

public final class RowBuilder {

    private final Headers headers;
    private final List<String> values;

    /**
     * Creates a new builder for the given headers.
     *
     * @param headers the headers describing the row shape (must not be null)
     */
    public RowBuilder(Headers headers) {
        if (headers == null) {
            throw new IllegalArgumentException("Headers cannot be null");
        }
        this.headers = headers;
        this.values = new ArrayList<>();
    }

    /**
     * Convenience factory method.
     */
    public static RowBuilder forHeaders(Headers headers) {
        return new RowBuilder(headers);
    }

    /**
     * @return the headers this builder is associated with.
     */
    public Headers getHeaders() {
        return headers;
    }

    /**
     * Appends a value for the next column in order.
     * <p>
     * Values are added in the same order as the header columns.
     *
     * @param value the cell value (may be null to represent missing data)
     * @return this builder for chaining
     */
    public RowBuilder add(String value) {
        values.add(value);
        return this;
    }

    /**
     * Appends all values for this row in one call.
     *
     * @param rowValues list of values (may contain nulls)
     * @return this builder for chaining
     */
    public RowBuilder addAll(List<String> rowValues) {
        if (rowValues == null) {
            throw new IllegalArgumentException("rowValues cannot be null");
        }
        values.addAll(rowValues);
        return this;
    }

    /**
     * Sets a value at the given column index.
     * <p>
     * If the internal list is not large enough yet, it will be grown
     * and intermediate positions filled with null.
     *
     * @param index zero-based column index
     * @param value cell value (may be null)
     * @return this builder for chaining
     */
    public RowBuilder set(int index, String value) {
        if (index < 0 || index >= headers.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        // Grow list up to index if needed
        while (values.size() <= index) {
            values.add(null);
        }
        values.set(index, value);
        return this;
    }

    /**
     * Sets a value by column name.
     *
     * @param columnName header name (case-insensitive)
     * @param value      cell value (may be null)
     * @return this builder for chaining
     */
    public RowBuilder set(String columnName, String value) {
        int idx = headers.getIndex(columnName);
        return set(idx, value);
    }

    /**
     * @return current number of values added to this builder.
     */
    public int size() {
        return values.size();
    }

    /**
     * @return true if the builder has exactly as many values as there are headers.
     */
    public boolean isComplete() {
        return values.size() == headers.size();
    }

    /**
     * Returns an unmodifiable view of the current values.
     * Useful for debugging/tests.
     */
    public List<String> getValues() {
        return Collections.unmodifiableList(values);
    }

    /**
     * Clears all collected values so this builder can be reused
     * for another row with the same headers.
     */
    public void clear() {
        values.clear();
    }

    /**
     * Builds an immutable {@link Row} from the current values.
     *
     * @return a new Row instance
     * @throws IllegalStateException if the number of values does not
     *                               match the number of header columns
     */
    public Row build() {
        if (!isComplete()) {
            throw new IllegalStateException(
                    "Cannot build Row: expected " + headers.size()
                            + " values but got " + values.size()
            );
        }
        // Row copies the list internally, so we can pass our mutable list.
        return new Row(headers, values);
    }
}
