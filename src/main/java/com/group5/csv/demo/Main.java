package com.group5.csv.demo;

import com.group5.csv.io.*;
import com.group5.csv.core.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Console-based demo entry point for the CSV Data Processor project.
 * <p>
 * This class exposes a simple text menu that allows the user to:
 * </p>
 * <ul>
 *     <li>Experiment with {@link CsvReader} configuration (charset, headers, dialect, etc.)</li>
 *     <li>Run round-trip demonstrations for different CSV dialects
 *         (comma, semicolon, tab-delimited)</li>
 *     <li>Generate a small placeholder CSV file for quick testing</li>
 * </ul>
 * <p>
 * It is intended as a lightweight developer and examiner-facing tool to
 * showcase the core capabilities of the CSV engine.
 * </p>
 */
public class Main {

    private static Headers lastHeaders;
    private static List<Row> lastRows;

    /**
     * Application entry point. Displays the main menu and dispatches user
     * choices to the appropriate demo methods until the user chooses to exit.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== CSV Data Processor Demo ===");

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1) Read CSV file");
            System.out.println("2) Write sample CSV");
            System.out.println("3) Validate CSV (Schema placeholder)");
            System.out.println("4) Round-trip CSV (comma: RFC-4180)");
            System.out.println("5) Round-trip CSV (semicolon: Excel dialect)");
            System.out.println("6) Round-trip CSV (tab-delimited: TSV)");

            System.out.println("0) Exit");
            System.out.print("Choose option: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> demoReadMenu(sc);
                case "2" -> demoWrite();
                case "3" -> demoValidateCsv();
                case "4" -> demoRoundTripComma(sc);
                case "5" -> demoRoundTripSemicolon(sc);
                case "6" -> demoRoundTripTab(sc);

                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }


    /**
     * Placeholder demonstration that writes a minimal CSV file using NIO only.
     * <p>
     * This does not use {@link CsvWriter}; it exists to provide a very simple
     * "write a file" example alongside the richer round-trip demos.
     * </p>
     */
    private static void demoWrite() {
        Path out = Paths.get("demo_output.csv");
        try {
            Files.write(out, List.of("id,name,age", "1,John,20", "2,Bob,30"));
            System.out.println("Created demo_output.csv (placeholder). Full CsvWriter integration pending.");
        } catch (IOException e) {
            System.out.println("Could not write demo_output.csv (placeholder mode): " + e.getMessage());
        }
    }


    // ----- CSV read demonstration -----
    /**
     * Reads a CSV file using the provided configuration and headers,
     * prints the total number of rows and previews the first 5.
     *
     * @param filePath path to the CSV file
     * @param config   CSV configuration to use
     * @param headers  optional custom headers (overrides file header)
     * @param maxLines  output lines limit, or unlimited if maxLines < 0
     */
//    private static void demoRead(String filePath, CsvConfig config, Headers headers, long maxLines) {
//        Path path = Path.of(filePath);
//
//        try (CsvReader reader = CsvReader.fromPath(path, config, headers)) {
//            List<Row> rows = reader.readAll();
//            System.out.printf("Read %d rows.%n", rows.size());
//            if (maxLines < 0)
//                rows.forEach(System.out::println);
//            else
//                rows.stream().limit(maxLines).forEach(System.out::println);
//        } catch (Exception e) {
//            System.out.println("Error reading CSV: " + e.getMessage());
//        }
//    }
private static void demoRead(String filePath, CsvConfig config, Headers headers, long maxLines) {
        Path path = Path.of(filePath);

        try (CsvReader reader = CsvReader.fromPath(path, config, headers)) {
            List<Row> rows = reader.readAll();

            // Remember the last CSV for validation
            lastRows = rows;

            // Try to use headers from the reader (file header or override)
            Headers effectiveHeaders = reader.getHeaders();

            // If there are no headers (e.g. hasHeader = false), synthesize some
            if (effectiveHeaders == null && !rows.isEmpty()) {
                effectiveHeaders = new Headers(rows.get(0).size());
            }
            lastHeaders = effectiveHeaders;

            System.out.printf("Read %d rows.%n", rows.size());

            // Use CsvTableFormatter to display the data as a table
            if (!rows.isEmpty()) {
                CsvTableFormatter formatter = new CsvTableFormatter(reader);

                // If maxLines is specified, only show limited rows
                List<Row> rowsToDisplay = maxLines < 0 ? rows :
                        rows.stream().limit(maxLines).toList();

                String table = formatter.formatTable(rowsToDisplay);
                System.out.println(table);

                // Show message if rows were truncated
                if (maxLines >= 0 && rows.size() > maxLines) {
                    System.out.printf("(Showing first %d of %d rows)%n", maxLines, rows.size());
                }
            }

        } catch (Exception e) {
            System.out.println("Error reading CSV: " + e.getMessage());
        }
    }


    /**
     * Interactive menu for configuring and testing {@link CsvReader}.
     * <p>
     * Allows the user to adjust:
     * </p>
     * <ul>
     *     <li>Input file path</li>
     *     <li>Charset</li>
     *     <li>Header presence and manual header override</li>
     *     <li>Empty-line skipping</li>
     *     <li>CSV dialect ({@link CsvFormat})</li>
     *     <li>Maximum number of lines to display</li>
     * </ul>
     *
     * @param sc scanner for user input
     */
    private static void demoReadMenu(Scanner sc) {
        String path = "src/main/resources/demo/demo_input.csv";
        CsvConfig config = CsvConfig.builder().build();
        Headers headers = null;
        CsvDialect dialect = CsvDialect.RFC4180;
        long maxLines = 5;

        while (true) {

            System.out.println("\nMenu -> Read File:");
            System.out.println("1) Read file");
            System.out.printf("2) Change file path (current=\"%s\")%n", path);
            System.out.printf("3) Change charset (current=%s)%n", config.getCharset());
            System.out.printf("4) Change header presence (current=%s)%n", config.hasHeader());
            System.out.printf("5) Override header (current=%s)%n", headers == null ? "No" : headers);
            System.out.printf("6) Change skip empty lines (current=%s)%n", config.isSkipEmptyLines());
            System.out.printf("7) Change CSV dialect (current=%s)%n", dialect);
            System.out.printf("8) Change line output limit (current=%s)%n",
                    maxLines < 0 ? "Unlimited" : maxLines);
            System.out.println("0) Back to main menu");
            System.out.print("Choose option: ");

            String choice = sc.nextLine().trim();

            if (!choice.matches("\\d+")) {
                System.out.println("Invalid input. Please enter a number.");
                continue; // IMPORTANT: error first, then menu
            }

            switch (choice) {
                case "1" -> demoRead(path, config, headers, maxLines);

                case "2" -> path = prompt(sc, "Enter CSV file path: ");

                case "3" -> config = handleCharsetChange(sc, config);

                case "4" -> {
                    boolean hasHeader = askYesNo(sc, "Read headers from file (y/n): ");
                    if (!hasHeader) {
                        headers = null;
                    }
                    config = config.toBuilder().setHasHeader(hasHeader).build();
                }

                case "5" -> {
                    Headers newHeaders = handleHeaderOverride(sc);
                    if (newHeaders != null) {
                        headers = newHeaders;
                        config = config.toBuilder().setHasHeader(true).build();
                    }
                }

                case "6" -> {
                    boolean skip = askYesNo(sc, "Skip empty lines (y/n): ");
                    config = config.toBuilder().setSkipEmptyLines(skip).build();
                }

                case "7" -> {
                    CsvDialectResult result = handleDialectChange(sc);
                    if (result != null) {
                        dialect = result.dialect();
                        config = config.toBuilder().setFormat(result.format()).build();
                    }
                }

                case "8" -> maxLines = handleMaxLines(sc, maxLines);

                case "0" -> { return; }

                default -> System.out.println("Unknown option.");
            }
        }
    }


    // ----- CSV read demonstration Helpers -----

    /**
     * Supported high-level CSV dialect labels used by the demo menu.
     * <p>
     * These map to concrete {@link CsvFormat} presets such as
     * {@link CsvFormat#rfc4180()}, {@link CsvFormat#excel()},
     * {@link CsvFormat#excel_semicolon()}, {@link CsvFormat#tsv()},
     * and {@link CsvFormat#json_csv()}.
     * </p>
     */
    private enum CsvDialect {
        RFC4180, EXCEL, EXCEL_SEMICOLON, TSV, JSON_CSV
    }

    /**
     * Handles interactive charset changes for {@link CsvConfig} in the
     * reader demo menu.
     *
     * @param sc     scanner for user input
     * @param config current CSV configuration
     * @return a new {@link CsvConfig} with the requested charset,
     *         or the original config if the charset is invalid
     */
    private static CsvConfig handleCharsetChange(Scanner sc, CsvConfig config) {
        System.out.println("Examples: US_ASCII, ISO_8859_1, UTF_8, UTF_16, UTF_32...");
        String cs = prompt(sc, "Enter file charset: ");

        try {
            return config.toBuilder()
                    .setCharset(Charset.forName(cs))
                    .build();
        } catch (Exception e) {
            System.out.println("Invalid charset: " + e.getMessage());
            return config;
        }
    }

    /**
     * Prompts the user for a column count and a sequence of column names,
     * then constructs a {@link Headers} instance.
     *
     * @param sc scanner for user input
     * @return a new {@link Headers} instance, or {@code null} if the input
     *         is invalid or the process is aborted
     */
    private static Headers handleHeaderOverride(Scanner sc) {
        String input = prompt(sc, "Enter column count: ");
        if (!input.matches("\\d+")) {
            System.out.println("Invalid input. Must be a number.");
            return null;
        }

        int count = Integer.parseInt(input);
        if (count <= 0) {
            System.out.println("Column count must be greater than 0.");
            return null;
        }

        List<String> columnNames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name = prompt(sc, "Enter column " + (i + 1) + " name: ");
            if (name.isEmpty()) {
                System.out.println("Column name cannot be empty.");
                i--; // repeat this iteration
                continue;
            }
            columnNames.add(name);
        }

        return new Headers(columnNames);
    }

    /**
     * Small record tying together a high-level dialect label and the
     * concrete {@link CsvFormat} instance used by the demo.
     */
    private record CsvDialectResult(CsvDialect dialect, CsvFormat format) {}

    /**
     * Handles interactive CSV dialect selection and returns the chosen
     * dialect together with its corresponding {@link CsvFormat}.
     *
     * @param sc scanner for user input
     * @return a {@link CsvDialectResult} containing the chosen dialect
     *         and format, or {@code null} if the user cancels or selects
     *         an unknown option
     */
    private static CsvDialectResult handleDialectChange(Scanner sc) {
        System.out.println("Choose CSV dialect:");
        System.out.println("1) RFC4180");
        System.out.println("2) EXCEL");
        System.out.println("3) EXCEL_SEMICOLON");
        System.out.println("4) TSV");
        System.out.println("5) JSON_CSV");
        System.out.println("0) Cancel");
        System.out.print("Your choice: ");

        String ans = sc.nextLine().trim();
        return switch (ans) {
            case "1" -> new CsvDialectResult(CsvDialect.RFC4180, CsvFormat.rfc4180());
            case "2" -> new CsvDialectResult(CsvDialect.EXCEL, CsvFormat.excel());
            case "3" -> new CsvDialectResult(CsvDialect.EXCEL_SEMICOLON, CsvFormat.excel_semicolon());
            case "4" -> new CsvDialectResult(CsvDialect.TSV, CsvFormat.tsv());
            case "5" -> new CsvDialectResult(CsvDialect.JSON_CSV, CsvFormat.json_csv());
            default -> null;
        };
    }

    /**
     * Prompts the user with a yes/no question and returns {@code true}
     * if the user answers with "y" (case-insensitive).
     *
     * @param sc     scanner for user input
     * @param prompt message to display to the user
     * @return {@code true} if the user answers "y", {@code false} otherwise
     */
    private static boolean askYesNo(Scanner sc, String prompt) {
        return prompt(sc, prompt).trim().equalsIgnoreCase("y");
    }

    /**
     * Utility method for prompting the user and reading a trimmed line
     * of input from the console.
     *
     * @param sc      scanner for user input
     * @param message message to display before reading input
     * @return the trimmed user input string (possibly empty)
     */
    private static String prompt(Scanner sc, String message) {
        System.out.print(message);
        return sc.nextLine().trim();
    }

    /**
     * Parses a user-supplied maximum line count, falling back to the
     * previous value if the input is not a valid integer.
     *
     * @param sc       scanner for user input
     * @param previous previous max line count to retain on error
     * @return the new max line count, or the previous value if parsing fails
     */
    private static long handleMaxLines(Scanner sc, long previous) {
        System.out.print("Enter max number of lines to display (negative for unlimited): ");
        String input = sc.nextLine().trim();
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Keeping previous value: " + previous);
            return previous;
        }
    }

    /**
     * Round-trip demo using tab-delimited CSV (TSV format).
     * Demonstrates {@link CsvReader} → {@link Row} → {@link CsvWriter} with {@link CsvFormat#tsv()}.
     * Uses a default demo file if the user leaves the input path blank.
     *
     * @param sc scanner for user input
     */
    private static void demoRoundTripTab(Scanner sc) {
        System.out.print("Enter input TSV file path (blank = demo_input_tab.tsv): ");
        String inPathStr = sc.nextLine().trim();

        if (inPathStr.isEmpty()) {
            inPathStr = "src/main/resources/demo/demo_input_tab.tsv";
            System.out.println("Using default demo file: " + inPathStr);
        }

        Path inPath = Paths.get(inPathStr);
        if (!Files.exists(inPath)) {
            System.out.println("Input file not found: " + inPath);
            return;
        }

        System.out.print("Enter output filename (blank = demo_output_tab.csv): ");
        String outPathStr = sc.nextLine().trim();
        if (outPathStr.isEmpty()) outPathStr = "demo_output_tab.csv";
        Path outPath = Paths.get(outPathStr);

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.tsv())
                .setHasHeader(true)
                .setRequireUniformFieldCount(true)
                .build();

        try (CsvReader reader = new CsvReader(
                Files.newBufferedReader(inPath, config.getCharset()), config)) {

            List<Row> rows = reader.readAll();
            System.out.printf("Read %d row(s)%n", rows.size());
            rows.stream().limit(5).forEach(System.out::println);

            try (CsvWriter writer = CsvWriter.toPath(outPath, config)) {
                writer.writeAllRows(rows);
            }

            System.out.println("Done! Wrote to: " + outPath);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    /**
     * Round-trip demo using semicolon-delimited CSV (Excel-style dialect).
     * Demonstrates {@link CsvReader} → {@link Row} → {@link CsvWriter} with
     * {@link CsvFormat#excel_semicolon()}. Uses a default demo file if the user
     * leaves the input path blank.
     *
     * @param sc scanner for user input
     */
    private static void demoRoundTripSemicolon(Scanner sc) {
        System.out.print("Enter input CSV path (blank = demo_input_semicolon.csv): ");
        String inPathStr = sc.nextLine().trim();

        if (inPathStr.isEmpty()) {
            inPathStr = "src/main/resources/demo/demo_input_semicolon.csv";
            System.out.println("Using default demo file: " + inPathStr);
        }

        Path inPath = Paths.get(inPathStr);
        if (!Files.exists(inPath)) {
            System.out.println("Input file not found: " + inPath);
            return;
        }

        System.out.print("Enter output file name (blank = demo_output_semicolon.csv): ");
        String outPathStr = sc.nextLine().trim();
        if (outPathStr.isEmpty()) outPathStr = "demo_output_semicolon.csv";
        Path outPath = Paths.get(outPathStr);

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.excel_semicolon())
                .setHasHeader(true)
                .setRequireUniformFieldCount(true)
                .build();

        try (CsvReader reader = new CsvReader(
                Files.newBufferedReader(inPath, config.getCharset()), config)) {

            List<Row> rows = reader.readAll();
            System.out.printf("Read %d row(s)%n", rows.size());
            rows.stream().limit(5).forEach(System.out::println);

            try (CsvWriter writer = CsvWriter.toPath(outPath, config)) {
                writer.writeAllRows(rows);
            }

            System.out.println("Done! Wrote to: " + outPath);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    /**
     * Demonstrates a simple CSV round-trip using a comma-delimited
     * RFC-4180 dialect:
     * <ul>
     *     <li>Reads rows with {@link CsvReader}</li>
     *     <li>Displays a small preview in the console</li>
     *     <li>Writes all rows back out with {@link CsvWriter}</li>
     * </ul>
     * Uses a default demo file if the user leaves the input path blank.
     *
     * @param sc scanner for user input
     */
    private static void demoRoundTripComma(Scanner sc) {
        System.out.print("Enter input CSV file path (leave blank for demo_input_comma.csv): ");
        String inPathStr = sc.nextLine().trim();

        if (inPathStr.isEmpty()) {
            inPathStr = "src/main/resources/demo/demo_input_comma.csv";
            System.out.println("Using default demo file: " + inPathStr);
        }

        Path inPath = Paths.get(inPathStr);
        if (!Files.exists(inPath)) {
            System.out.println("Input file not found: " + inPath);
            return;
        }

        System.out.print("Enter output CSV file path (blank = demo_output_comma.csv): ");
        String outPathStr = sc.nextLine().trim();
        if (outPathStr.isEmpty()) outPathStr = "demo_output_comma.csv";
        Path outPath = Paths.get(outPathStr);

        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.rfc4180())
                .setHasHeader(true)
                .setRequireUniformFieldCount(true)
                .build();

        try (CsvReader reader = new CsvReader(
                Files.newBufferedReader(inPath, config.getCharset()), config)) {

            List<Row> rows = reader.readAll();
            System.out.printf("Read %d row(s)%n", rows.size());
            rows.stream().limit(5).forEach(System.out::println);

            try (CsvWriter writer = CsvWriter.toPath(outPath, config)) {
                writer.writeAllRows(rows);
            }

            System.out.println("Done! Wrote to: " + outPath);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void demoValidateCsv() {
        if (lastRows == null || lastHeaders == null) {
            System.out.println("No CSV loaded. Please read a CSV first.");
            return;
        }

        boolean successes = true;
        for (Row row : lastRows) {
            if (row.size() != lastHeaders.size()) {
                System.out.println("Row length mismatch: " + row);
                successes = false;
            }
        }

        if (successes) {
            System.out.println("CSV appears structurally valid.");
        }
    }


}
