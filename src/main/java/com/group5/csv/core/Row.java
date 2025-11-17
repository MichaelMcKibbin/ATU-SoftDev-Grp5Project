package com.group5.csv.core;

import java.util.*;

/**
 * Represents a single row of CSV data with values.
 * <p>
 * Provides fast access to values by column name (via {@link Headers}) or by index.
 * Instances are immutable and thread-safe. Null values are allowed to represent
 * missing data in CSV files.
 * </p>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Headers headers = new Headers("id", "name", "age");
 * Row row = new Row(headers, Arrays.asList("1", "Alice", "25"));
 * 
 * String name = row.get("name");  // "Alice"
 * String age = row.get(2);        // "25"
 * }</pre>
 * 
 * @author Edson Ferreira
 * @see Headers
 * @see <a href="https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/issues/10">Issue #10</a>
 */
public class Row {
    private final Headers headers;
    private final List<String> values;
    
    /**
     * Creates a Row instance with the specified headers and values.
     * <p>
     * The number of values must exactly match the number of columns in the headers.
     * Individual values may be null to represent missing data.
     * </p>
     * 
     * @param headers the column headers (must not be null)
     * @param values the row values (must not be null, can contain null elements)
     * @throws IllegalArgumentException if headers is null
     * @throws IllegalArgumentException if values is null
     * @throws IllegalArgumentException if value count does not match header count
     */
    public Row(Headers headers, List<String> values) {
        if (headers == null) {
            throw new IllegalArgumentException("Headers cannot be null");
        }
        if (values == null) {
            throw new IllegalArgumentException("Values cannot be null");
        }
        if (values.size() != headers.size()) {
            throw new IllegalArgumentException(
                "Value count (" + values.size() + ") does not match header count (" + headers.size() + ")"
            );
        }
        
        this.headers = headers;
        this.values = new ArrayList<>(values);
    }
    
    /**
     * Gets the value for the specified column name.
     * <p>
     * Uses the associated {@link Headers} to resolve the column name to an index.
     * Column name lookup is case-insensitive and whitespace-trimmed.
     * </p>
     * 
     * @param columnName the name of the column (must not be null)
     * @return the value at that column, or null if the value is missing
     * @throws IllegalArgumentException if the column name does not exist
     */
    public String get(String columnName) {
        int index = headers.getIndex(columnName);
        return values.get(index);
    }
    
    /**
     * Gets the value at the specified column index.
     * 
     * @param index the zero-based column index
     * @return the value at that index, or null if the value is missing
     * @throws IndexOutOfBoundsException if index is negative or >= size()
     */
    public String get(int index) {
        if (index < 0 || index >= values.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        return values.get(index);
    }
    
    /**
     * Gets the number of values in this row.
     * 
     * @return the number of values
     */
    public int size() {
        return values.size();
    }
    
    /**
     * Checks if this row has no values.
     * 
     * @return true if size is 0, false otherwise
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }
    
    /**
     * Gets all values in this row as an unmodifiable list.
     * <p>
     * The returned list preserves the original order and may contain null elements.
     * Attempts to modify the list will throw {@link UnsupportedOperationException}.
     * </p>
     * 
     * @return an unmodifiable list of all values in this row
     */
    public List<String> getValues() {
        return Collections.unmodifiableList(values);
    }
    
    /**
     * Gets the headers associated with this row.
     * 
     * @return the headers
     */
    public Headers getHeaders() {
        return headers;
    }
}
