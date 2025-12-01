package com.group5.csv.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private PrintStream originalOut;
    private PrintStream originalErr;
    private InputStream originalIn;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;


    @BeforeEach
    void setUp() {
        originalOut = System.out;
        originalErr = System.err;
        originalIn = System.in;

        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(errContent, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(originalIn);

        outContent.close();
        errContent.close();
    }


    /**
     * Drive Main.main() with an invalid choice followed by 0 (exit).
     * This covers:
     *  - main loop
     *  - "Invalid choice" branch
     *  - "0" (exit) branch.
     */
    @Test
    void main_invalidChoiceThenExit() throws Exception {
        // First line: invalid menu option
        // Second line: 0 (exit)
        String inputScript = String.join("\n",
                "9",   // invalid
                "0"    // exit
        ) + "\n";

        System.setIn(new ByteArrayInputStream(inputScript.getBytes(StandardCharsets.UTF_8)));

        Main.main(new String[0]);

        String output = outContent.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("CSV Data Processor Demo"),
                "Should print banner");
        assertTrue(output.contains("Invalid choice"),
                "Should warn about invalid choice");
        assertTrue(output.contains("Goodbye!"),
                "Should print goodbye on exit");
    }

    // --- Helpers to call private methods via reflection ---

    private void invokeDemoReadMenuWithInput(String script) throws Exception {
        Method method = Main.class.getDeclaredMethod("demoReadMenu", Scanner.class);
        method.setAccessible(true);

        ByteArrayInputStream in = new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
        Scanner scanner = new Scanner(in);

        method.invoke(null, scanner); // static method => null instance
    }

    /**
     * Ensures the numeric validation branch in demoReadMenu is hit:
     * - first input is non-numeric => "Invalid input" path
     * - then 0 to return to main menu.
     */
    @Test
    void demoReadMenu_rejectsNonNumericChoice() throws Exception {
        String script = String.join("\n",
                "abc", // invalid (non-numeric)
                "0"    // exit read menu
        ) + "\n";

        invokeDemoReadMenuWithInput(script);

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Invalid input. Please enter a number."),
                "Non-numeric choices should be rejected");
    }

    /**
     * Walks through most of the read-menu options with valid inputs.
     * This gives coverage on the new menu cases (2–8) and their helpers.
     *
     * We don't assert internal state here; success is "no exception" and
     * the expected prompts appearing in the output.
     */
    @Test
    void demoReadMenu_traversesMostOptions() throws Exception {
        String script = String.join("\n",
                "2", "custom.csv",          // change file path
                "3", "UTF-8",               // change charset
                "4", "y",                   // has header = true
                "5", "2", "first", "second",// override headers: 2 columns
                "6", "n",                   // skip empty lines = false
                "7", "1",                   // dialect: RFC4180
                "8", "10",                  // max lines = 10
                "0"                         // back to main menu
        ) + "\n";

        invokeDemoReadMenuWithInput(script);

        String output = outContent.toString(StandardCharsets.UTF_8);

        // These checks are mainly sanity checks that the menu loop ran.
        assertTrue(output.contains("Menu -> Read File:"),
                "Read-menu header should be printed");
        assertTrue(output.contains("Change file path"),
                "Option 2 should be shown");
        assertTrue(output.contains("Change charset"),
                "Option 3 should be shown");
        assertTrue(output.contains("Change header presence"),
                "Option 4 should be shown");
        assertTrue(output.contains("Override header"),
                "Option 5 should be shown");
        assertTrue(output.contains("Change skip empty lines"),
                "Option 6 should be shown");
        assertTrue(output.contains("Change CSV dialect"),
                "Option 7 should be shown");
        assertTrue(output.contains("Change line output limit"),
                "Option 8 should be shown");
    }

    /**
     * Directly exercises handleMaxLines via reflection to cover the
     * "invalid number, keep previous value" branch.
     */
    @Test
    void handleMaxLines_invalidInputKeepsPrevious() throws Exception {
        Method method = Main.class.getDeclaredMethod("handleMaxLines", Scanner.class, long.class);
        method.setAccessible(true);

        String script = "not-a-number\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)));

        long result = (long) method.invoke(null, scanner, 5L);

        assertEquals(5L, result,
                "Invalid input should keep previous maxLines value");

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Invalid input. Keeping previous value: 5"),
                "Should log that previous value is being kept");
    }





    @Test
    void main_roundTripOption_withExplicitOutputPath() throws Exception {
        // Create a temp directory + input CSV
        Path tempDir = Files.createTempDirectory("roundtrip-main-explicit");
        Path inputCsv = tempDir.resolve("input.csv");
        List<String> lines = List.of(
                "id,name,age",
                "1,Alice,30",
                "2,Bob,25"
        );
        Files.write(inputCsv, lines, StandardCharsets.UTF_8);

        Path outputCsv = tempDir.resolve("output.csv");

        // Scripted "user input":
        //  1) "4" -> choose round-trip option
        //  2) input path
        //  3) output path
        //  4) "0" -> exit main menu
        String script = String.join(System.lineSeparator(),
                "4",
                inputCsv.toString(),
                outputCsv.toString(),
                "0"
        ) + System.lineSeparator();

        System.setIn(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)));

        // Run the real entry point
        Main.main(new String[0]);

        // Assert: output file exists
        assertTrue(Files.exists(outputCsv), "Output CSV should be created by round-trip option 4");

        // Because config.setHasHeader(true), CsvReader treats the first line
        // as header metadata and CsvWriter only writes the data rows.
        List<String> outLines = Files.readAllLines(outputCsv, StandardCharsets.UTF_8);
        assertEquals(
                lines.size() - 1,
                outLines.size(),
                "Round-tripped file should have same number of data lines as input (header is not re-written)"
        );

        String stdout = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(stdout.contains("Round-trip complete."),
                "Console output should indicate completion");

        // Cleanup
        Files.deleteIfExists(inputCsv);
        Files.deleteIfExists(outputCsv);
        Files.deleteIfExists(tempDir);
    }


    @Test
    void main_roundTripOption_withBlankOutputPath_usesDefault() throws Exception {
        // Create a temporary input CSV (absolute path)
        Path inputCsv = Files.createTempFile("roundtrip-main-default-input", ".csv");
        List<String> lines = List.of(
                "id,name,age",
                "1,Charlie,22",
                "2,Dana,35"
        );
        Files.write(inputCsv, lines, StandardCharsets.UTF_8);

        // demoRoundTrip uses "demo_output.csv" in the working directory when the user
        // leaves the output prompt blank.
        Path defaultOut = Path.of("demo_output.csv");
        Files.deleteIfExists(defaultOut); // avoid interference from previous runs

        // Scripted input:
        //  1) "4" -> round-trip option
        //  2) absolute path to input CSV
        //  3) "" (blank) -> default output path
        //  4) "0" -> exit main menu
        String script = String.join(System.lineSeparator(),
                "4",
                inputCsv.toString(),
                "",
                "0"
        ) + System.lineSeparator();

        System.setIn(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)));

        Main.main(new String[0]);

        // Now demoRoundTrip should have created demo_output.csv in the working directory
        assertTrue(Files.exists(defaultOut),
                "demo_output.csv should be created when output path is left blank");

        List<String> outLines = Files.readAllLines(defaultOut, StandardCharsets.UTF_8);
        assertEquals(
                lines.size() - 1,
                outLines.size(),
                "Default-output round-trip should preserve number of data rows (header not re-written)"
        );

        String stdout = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(stdout.contains("demo_output.csv"),
                "Console output should mention the default output path");
        assertTrue(stdout.contains("Round-trip complete."),
                "Console output should indicate completion");

        // Cleanup
        Files.deleteIfExists(inputCsv);
        Files.deleteIfExists(defaultOut);
    }



    /**
     * old test
     */

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
