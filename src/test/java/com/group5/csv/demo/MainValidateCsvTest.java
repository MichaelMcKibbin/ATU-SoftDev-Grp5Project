// src/test/java/com/group5/csv/demo/MainValidateCsvTest.java
package com.group5.csv.demo;

import com.group5.csv.core.Headers;
import com.group5.csv.core.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainValidateCsvTest {

    private PrintStream originalOut;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // --- helper to set private static fields on Main ---

    private static void setStaticField(String fieldName, Object value) throws Exception {
        Field f = Main.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);  // static field => null instance
    }

    private static void invokeDemoValidateCsv() throws Exception {
        Method m = Main.class.getDeclaredMethod("demoValidateCsv");
        m.setAccessible(true);
        m.invoke(null);      // static method => null instance
    }

    @Test
    void validateCsv_noCsvLoaded_printsFriendlyMessage() throws Exception {
        // Ensure both are null
        setStaticField("lastHeaders", null);
        setStaticField("lastRows", null);

        invokeDemoValidateCsv();

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("No CSV loaded"),
                "Should warn the user when no CSV has been loaded");
    }

    @Test
    void validateCsv_allRowsMatchHeader_printsSuccessMessage() throws Exception {
        Headers headers = new Headers("id", "name", "age");

        // All rows have exactly 3 values
        List<Row> rows = List.of(
                new Row(headers, List.of("1", "Alice", "30")),
                new Row(headers, List.of("2", "Bob", "40")),
                new Row(headers, List.of("3", "Charlie", "50"))
        );

        setStaticField("lastHeaders", headers);
        setStaticField("lastRows", rows);

        invokeDemoValidateCsv();

        String output = outContent.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("CSV appears structurally valid."),
                "Should print success message when all rows match header length");
        assertFalse(output.contains("Row length mismatch"),
                "Should not print mismatch warnings for valid CSV");
    }

    @Test
    void validateCsv_mismatchedRowLengths_printsMismatchWarnings() throws Exception {
        // Headers actually used when building the rows (2 columns)
        Headers rowHeaders = new Headers("id", "name");

        // All rows are internally consistent: 2 headers, 2 values
        List<Row> rows = List.of(
                new Row(rowHeaders, List.of("1", "Alice")),
                new Row(rowHeaders, List.of("2", "Bob")),
                new Row(rowHeaders, List.of("3", "Charlie"))
        );

        // But the "expected" headers for validation claim there should be 3 columns
        Headers validationHeaders = new Headers("id", "name", "age");

        // Wire up the demo state
        setStaticField("lastHeaders", validationHeaders);
        setStaticField("lastRows", rows);

        // Run the validation
        invokeDemoValidateCsv();

        String output = outContent.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("Row length mismatch"),
                "Should print at least one row length mismatch warning");
        assertFalse(output.contains("CSV appears structurally valid."),
                "Should NOT print success message when mismatches exist");
    }


}
