package com.group5.csv.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Unit tests for the Headers class.
 * 
 * @author Edson Ferreira
 */
class HeadersTest {
    
    // Constructor Tests
    
    @Test
    void shouldCreateHeadersFromList() {
        List<String> columns = Arrays.asList("id", "name", "age");
        Headers headers = new Headers(columns);
        assertEquals(3, headers.size());
    }
    
    @Test
    void shouldThrowExceptionForNullList() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Headers(null);
        });
    }
    
    @Test
    void shouldThrowExceptionForEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Headers(new ArrayList<>());
        });
    }
    
    @Test
    void shouldThrowExceptionForNullColumnName() {
        List<String> columns = Arrays.asList("id", null, "age");
        assertThrows(IllegalArgumentException.class, () -> {
            new Headers(columns);
        });
    }
    
    @Test
    void shouldThrowExceptionForDuplicateColumns() {
        List<String> columns = Arrays.asList("id", "name", "id");
        assertThrows(IllegalArgumentException.class, () -> {
            new Headers(columns);
        });
    }
    
    @Test
    void shouldThrowExceptionForEmptyStringColumnName() {
        List<String> columns = Arrays.asList("id", "", "name");
        assertThrows(IllegalArgumentException.class, () -> {
            new Headers(columns);
        });
    }
    
    @Test
    void shouldThrowExceptionForWhitespaceOnlyColumnName() {
        List<String> columns = Arrays.asList("id", "   ", "name");
        assertThrows(IllegalArgumentException.class, () -> {
            new Headers(columns);
        });
    }
    
    @Test
    void shouldThrowExceptionForTabOnlyColumnName() {
        List<String> columns = Arrays.asList("id", "\t\t", "name");
        assertThrows(IllegalArgumentException.class, () -> {
            new Headers(columns);
        });
    }
    
    @Test
    void shouldThrowExceptionForMixedWhitespaceOnlyColumnName() {
        List<String> columns = Arrays.asList("id", " \t \n ", "name");
        assertThrows(IllegalArgumentException.class, () -> {
            new Headers(columns);
        });
    }
    
    // Whitespace Trimming Tests
    
    @Test
    void shouldTrimLeadingWhitespace() {
        Headers headers = new Headers(Arrays.asList("  id", "name", "age"));
        assertEquals(0, headers.getIndex("id"));
        assertEquals("id", headers.getName(0));
    }
    
    @Test
    void shouldTrimTrailingWhitespace() {
        Headers headers = new Headers(Arrays.asList("id  ", "name", "age"));
        assertEquals(0, headers.getIndex("id"));
        assertEquals("id", headers.getName(0));
    }
    
    @Test
    void shouldTrimBothSidesWhitespace() {
        Headers headers = new Headers(Arrays.asList("  id  ", "  name  ", "  age  "));
        assertEquals("id", headers.getName(0));
        assertEquals("name", headers.getName(1));
        assertEquals("age", headers.getName(2));
    }
    
    @Test
    void shouldDetectDuplicatesAfterTrimming() {
        List<String> columns = Arrays.asList("id", "  id  ", "name");
        assertThrows(IllegalArgumentException.class, () -> {
            new Headers(columns);
        });
    }
    
    // Case-Insensitive Lookup Tests
    
    @Test
    void shouldFindColumnWithDifferentCase() {
        Headers headers = new Headers(Arrays.asList("id", "Name", "AGE"));
        assertEquals(0, headers.getIndex("ID"));
        assertEquals(1, headers.getIndex("name"));
        assertEquals(2, headers.getIndex("age"));
    }
    
    @Test
    void shouldContainColumnWithDifferentCase() {
        Headers headers = new Headers(Arrays.asList("id", "Name", "AGE"));
        assertTrue(headers.contains("ID"));
        assertTrue(headers.contains("name"));
        assertTrue(headers.contains("age"));
    }
    
    @Test
    void shouldDetectDuplicatesIgnoringCase() {
        List<String> columns = Arrays.asList("id", "Name", "ID");
        assertThrows(IllegalArgumentException.class, () -> {
            new Headers(columns);
        });
    }
    
    @Test
    void shouldPreserveOriginalCaseInGetName() {
        Headers headers = new Headers(Arrays.asList("ID", "Name", "AGE"));
        assertEquals("ID", headers.getName(0));
        assertEquals("Name", headers.getName(1));
        assertEquals("AGE", headers.getName(2));
    }
    
    // Combined Whitespace + Case Tests
    
    @Test
    void shouldTrimAndIgnoreCaseTogether() {
        Headers headers = new Headers(Arrays.asList("  ID  ", "  Name  ", "  AGE  "));
        assertEquals(0, headers.getIndex("id"));
        assertEquals(1, headers.getIndex("name"));
        assertEquals(2, headers.getIndex("age"));
        assertEquals("ID", headers.getName(0));
        assertEquals("Name", headers.getName(1));
        assertEquals("AGE", headers.getName(2));
    }
    
    // getIndex() Tests
    
    @Test
    void shouldGetIndexByName() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        assertEquals(0, headers.getIndex("id"));
        assertEquals(1, headers.getIndex("name"));
        assertEquals(2, headers.getIndex("age"));
    }
    
    @Test
    void shouldThrowExceptionForInvalidColumnName() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        assertThrows(IllegalArgumentException.class, () -> {
            headers.getIndex("email");
        });
    }
    
    // getName() Tests
    
    @Test
    void shouldGetNameByIndex() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        assertEquals("id", headers.getName(0));
        assertEquals("name", headers.getName(1));
        assertEquals("age", headers.getName(2));
    }
    
    @Test
    void shouldThrowExceptionForNegativeIndex() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        assertThrows(IndexOutOfBoundsException.class, () -> {
            headers.getName(-1);
        });
    }
    
    @Test
    void shouldThrowExceptionForIndexTooLarge() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        assertThrows(IndexOutOfBoundsException.class, () -> {
            headers.getName(10);
        });
    }
    
    // contains() Tests
    
    @Test
    void shouldReturnTrueForExistingColumn() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        assertTrue(headers.contains("name"));
    }
    
    @Test
    void shouldReturnFalseForNonExistingColumn() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        assertFalse(headers.contains("email"));
    }
    
    // size() Tests
    
    @Test
    void shouldReturnCorrectSize() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        assertEquals(3, headers.size());
    }
    
    @Test
    void shouldHandleSingleColumn() {
        Headers headers = new Headers(Arrays.asList("id"));
        assertEquals(1, headers.size());
        assertEquals(0, headers.getIndex("id"));
        assertEquals("id", headers.getName(0));
    }
    
    @Test
    void shouldHandleManyColumns() {
        List<String> columns = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            columns.add("column" + i);
        }
        Headers headers = new Headers(columns);
        assertEquals(100, headers.size());
        assertEquals(50, headers.getIndex("column50"));
        assertEquals("column99", headers.getName(99));
    }
    
    // getColumnNames() Tests
    
    @Test
    void shouldReturnAllColumnNames() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        List<String> columns = headers.getColumnNames();
        assertEquals(3, columns.size());
        assertEquals("id", columns.get(0));
        assertEquals("name", columns.get(1));
        assertEquals("age", columns.get(2));
    }
    
    @Test
    void shouldReturnUnmodifiableList() {
        Headers headers = new Headers(Arrays.asList("id", "name", "age"));
        List<String> columns = headers.getColumnNames();
        assertThrows(UnsupportedOperationException.class, () -> {
            columns.add("email");
        });
    }
    
    // Integration Test
    
    @Test
    void shouldWorkInCsvReaderWorkflow() {
        // Simulate CsvReader creating Headers
        String csvHeader = "id,name,age,city";
        List<String> columns = Arrays.asList(csvHeader.split(","));
        Headers headers = new Headers(columns);
        
        // Simulate Row using Headers  
        String[] rowData = {"1", "Alice", "25", "Dublin"};
        
        assertEquals("Alice", rowData[headers.getIndex("name")]);
        assertEquals("25", rowData[headers.getIndex("age")]);
        assertEquals("Dublin", rowData[headers.getIndex("city")]);
    }
}
