package com.group5.csv.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * This test only checks that:
 *
 * The program starts
 * No immediate crash occurs
 * It does NOT enter the loop (because it waits for input).
 * JUnit detects the hanging and stops execution automatically when main returns or fails.
 */

class MainTest {

    @Test
    void testMainRunsWithoutException() {
        // We don’t actually interact with the menu loop,
        // just ensure it starts cleanly.
        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }
}
