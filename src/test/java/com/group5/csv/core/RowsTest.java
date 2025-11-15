package com.group5.csv.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Unit tests for the Rows class.
 * 
 * @author Edson Ferreira
 */
class RowsTest {
    
    // Constructor Tests
    
    @Test
    void shouldCreateRowsWithHeadersAndValues() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", "25"));
        assertEquals(3, row.size());
    }
    
    @Test
    void shouldThrowExceptionForNullHeaders() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Rows(null, Arrays.asList("1", "Alice", "25"));
        });
    }
    
    @Test
    void shouldThrowExceptionForNullValues() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        assertThrows(IllegalArgumentException.class, () -> {
            new Rows(headers, null);
        });
    }
    
    @Test
    void shouldThrowExceptionForSizeMismatch() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        assertThrows(IllegalArgumentException.class, () -> {
            new Rows(headers, Arrays.asList("1", "Alice")); // Only 2 values
        });
    }
    
    @Test
    void shouldAllowNullValuesForMissingData() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", null));
        assertNull(row.get("age"));
    }
    
    // get(String columnName) Tests
    
    @Test
    void shouldGetValueByColumnName() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", "25"));
        
        assertEquals("1", row.get("id"));
        assertEquals("Alice", row.get("name"));
        assertEquals("25", row.get("age"));
    }
    
    @Test
    void shouldThrowExceptionForInvalidColumnName() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", "25"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            row.get("email");
        });
    }
    
    @Test
    void shouldGetNullValueByColumnName() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", null, "25"));
        
        assertNull(row.get("name"));
    }
    
    // get(int index) Tests
    
    @Test
    void shouldGetValueByIndex() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", "25"));
        
        assertEquals("1", row.get(0));
        assertEquals("Alice", row.get(1));
        assertEquals("25", row.get(2));
    }
    
    @Test
    void shouldThrowExceptionForNegativeIndex() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", "25"));
        
        assertThrows(IndexOutOfBoundsException.class, () -> {
            row.get(-1);
        });
    }
    
    @Test
    void shouldThrowExceptionForIndexTooLarge() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", "25"));
        
        assertThrows(IndexOutOfBoundsException.class, () -> {
            row.get(10);
        });
    }
    
    // size() Tests
    
    @Test
    void shouldReturnCorrectSize() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", "25"));
        
        assertEquals(3, row.size());
    }
    
    @Test
    void shouldHandleSingleValue() {
        Headers headers = new Headers(Arrays.asList("id"));
        Rows row = new Rows(headers, Arrays.asList("1"));
        
        assertEquals(1, row.size());
    }
    
    @Test
    void shouldHandleManyValues() {
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            columns.add("col" + i);
            values.add("val" + i);
        }
        
        Headers headers = new Headers(columns);
        Rows row = new Rows(headers, values);
        
        assertEquals(100, row.size());
        assertEquals("val50", row.get("col50"));
    }
    
    // isEmpty() Tests
    
    @Test
    void shouldReturnFalseForNonEmptyRow() {
        Headers headers = new Headers(Arrays.asList("id", "name"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice"));
        
        assertFalse(row.isEmpty());
    }
    
    // getValues() Tests
    
    @Test
    void shouldReturnAllValues() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", "25"));
        
        List<String> values = row.getValues();
        assertEquals(3, values.size());
        assertEquals("1", values.get(0));
        assertEquals("Alice", values.get(1));
        assertEquals("25", values.get(2));
    }
    
    @Test
    void shouldReturnUnmodifiableList() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", "25"));
        
        List<String> values = row.getValues();
        assertThrows(UnsupportedOperationException.class, () -> {
            values.add("extra");
        });
    }
    
    // getHeaders() Tests
    
    @Test
    void shouldReturnHeaders() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", "25"));
        
        assertEquals(headers, row.getHeaders());
    }
    
    // Integration Tests
    
    @Test
    void shouldWorkInCsvReaderWorkflow() {
        // Simulate CsvReader creating Headers and Rows
        String csvHeader = "id,name,age,city";
        List<String> columns = Arrays.asList(csvHeader.split(","));
        Headers headers = new Headers(columns);
        
        String csvRow = "1,Alice,25,Dublin";
        List<String> values = Arrays.asList(csvRow.split(","));
        Rows row = new Rows(headers, values);
        
        // Access data by column name
        assertEquals("Alice", row.get("name"));
        assertEquals("25", row.get("age"));
        assertEquals("Dublin", row.get("city"));
        
        // Access data by index
        assertEquals("1", row.get(0));
        assertEquals("Alice", row.get(1));
    }
    
    @Test
    void shouldHandleMessyRealWorldCsv() {
        // Messy header with whitespace: "  ID  ,Name, AGE ,City"
        // Messy data with missing value: "1,Alice,,Dublin"
        Headers headers = new Headers(Arrays.asList("  ID  ", "Name", " AGE ", "City"));
        Rows row = new Rows(headers, Arrays.asList("1", "Alice", null, "Dublin"));
        
        // Headers trims whitespace and is case-insensitive
        assertEquals("Alice", row.get("name"));      // lowercase works
        assertEquals("Alice", row.get("NAME"));      // uppercase works
        assertEquals("Alice", row.get("Name"));      // mixed case works
        assertEquals("1", row.get("id"));            // trimmed column name
        assertEquals("1", row.get("ID"));            // case-insensitive
        
        // Missing data returns null
        assertNull(row.get("age"));
        assertNull(row.get("AGE"));
    }
}
