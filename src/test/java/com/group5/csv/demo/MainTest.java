package com.group5.csv.demo;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void mainExitsWhenUserChoosesZero() {
        // Simulate user typing "0" + Enter to immediately exit the menu
        String fakeInput = "0\n";

        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayInputStream in = new ByteArrayInputStream(fakeInput.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        System.setIn(in);
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));

        try {
            assertDoesNotThrow(() -> Main.main(new String[]{}));

            String output = out.toString(StandardCharsets.UTF_8);

            // Optional assertions – nice to have but not strictly required
            assertTrue(output.contains("=== CSV Data Processor Demo ==="),
                    "Main menu banner should be printed");
            assertTrue(output.contains("Goodbye!"),
                    "Program should print Goodbye! when exiting");
        } catch (Exception e) {
            fail("Main.main should not throw when provided with option 0: " + e.getMessage());
        } finally {
            // Always restore the original streams so other tests are not affected
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }
}
