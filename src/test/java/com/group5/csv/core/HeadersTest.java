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
}
