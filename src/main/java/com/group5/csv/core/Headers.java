package com.group5.csv.core;

import java.util.*;

/**
 * Manages the mapping between CSV column names and their positional indexes.
 * Provides fast, bidirectional lookup for column access.
 * 
 * @author Edson Ferreira
 * @see <a href="https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/issues/11">Issue #11</a>
 */
public class Headers {
    private final List<String> columnNames;
    private final Map<String, Integer> nameToIndex;
    
    /**
     * Creates a Headers instance from a list of column names.
     * Column names are trimmed of leading/trailing whitespace.
     * Lookup is case-insensitive for better user experience.
     * 
     * @param columnNames ordered list of column names from CSV header
     * @throws IllegalArgumentException if columnNames is null, empty, contains null, 
     *                                  contains empty/whitespace-only strings, or has duplicates
     */
    public Headers(List<String> columnNames) {
        if (columnNames == null || columnNames.isEmpty()) {
            throw new IllegalArgumentException("Column names cannot be null or empty");
        }
        
        this.columnNames = new ArrayList<>();
        this.nameToIndex = new HashMap<>();
        
        for (int i = 0; i < columnNames.size(); i++) {
            String name = columnNames.get(i);
            if (name == null) {
                throw new IllegalArgumentException("Column name cannot be null");
            }
            
            String trimmed = name.trim();
            
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Column name cannot be empty or whitespace-only");
            }
            
            String normalized = trimmed.toLowerCase();
            
            if (nameToIndex.containsKey(normalized)) {
                throw new IllegalArgumentException("Duplicate column name: " + trimmed);
            }
            
            this.columnNames.add(trimmed);
            nameToIndex.put(normalized, i);
        }
    }
    
    /**
     * Gets the positional index of a column by its name.
     * Lookup is case-insensitive for better user experience.
     * 
     * @param columnName the name of the column to find
     * @return the zero-based index of the column
     * @throws IllegalArgumentException if column doesn't exist
     */
    public int getIndex(String columnName) {
        Integer index = nameToIndex.get(columnName.toLowerCase());
        if (index == null) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }
        return index;
    }
    
    /**
     * Gets the column name at a specific position.
     * 
     * @param index the zero-based position of the column
     * @return the name of the column at that position
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public String getName(int index) {
        if (index < 0 || index >= columnNames.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        return columnNames.get(index);
    }
    
    /**
     * Checks if a column exists in the headers.
     * Lookup is case-insensitive for better user experience.
     * 
     * @param columnName the name of the column to check
     * @return true if column exists, false otherwise
     */
    public boolean contains(String columnName) {
        return nameToIndex.containsKey(columnName.toLowerCase());
    }
    
    /**
     * Gets the total number of columns.
     * 
     * @return the number of columns in the headers
     */
    public int size() {
        return columnNames.size();
    }
    
    /**
     * Gets all column names in their original order.
     * 
     * @return unmodifiable list of column names
     */
    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }
}
