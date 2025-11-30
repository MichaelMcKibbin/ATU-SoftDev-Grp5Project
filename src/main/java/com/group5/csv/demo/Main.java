package com.group5.csv.demo;

import com.group5.csv.io.*;
import com.group5.csv.core.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== CSV Data Processor Demo ===");

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1) Read CSV file");
            System.out.println("2) Write sample CSV");
            System.out.println("3) Validate CSV (Schema placeholder)");
            System.out.println("4) Round-trip CSV (read & write using CsvReader/CsvWriter)");
            System.out.println("0) Exit");
            System.out.print("Choose option: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> demoReadMenu(sc);
                case "2" -> demoWrite();
                case "3" -> System.out.println("Schema validation not yet implemented.");
                case "4" -> demoRoundTrip(sc);
                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

//    /** Placeholder: previews the file without using CsvReader (keeps demo usable). */
//    private static void demoRead(Scanner sc) {
//        System.out.print("Enter CSV file path: ");
//        String path = sc.nextLine().trim();
//        Path p = Paths.get(path);
//        if (!Files.exists(p)) {
//            System.out.println("File not found. (CsvReader not fully implemented yet.)");
//            return;
//        }
//        try {
//            List<String> lines = Files.readAllLines(p); // placeholder
//            System.out.printf("Read %d line(s). Showing up to 5:%n", lines.size());
//            lines.stream().limit(5).forEach(System.out::println);
//            System.out.println("\nNote: This is a placeholder preview. Full CsvReader integration pending.");
//        } catch (IOException e) {
//            System.out.println("Could not read file (placeholder mode): " + e.getMessage());
//        }
//    }

    /** Placeholder: writes a minimal CSV using NIO (no CsvWriter yet). */
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
    private static void demoRead(String filePath, CsvConfig config, Headers headers, long maxLines) {
        Path path = Path.of(filePath);

        try (CsvReader reader = CsvReader.fromPath(path, config, headers)) {
            List<Row> rows = reader.readAll();
            System.out.printf("Read %d rows.%n", rows.size());
            if (maxLines < 0)
                rows.forEach(System.out::println);
            else
                rows.stream().limit(maxLines).forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Error reading CSV: " + e.getMessage());
        }
    }

    /**
     * Interactive menu for configuring and testing CsvReader.
     *
     * @param sc Scanner for user input
     */
    private static void demoReadMenu(Scanner sc) {
        String path = "demo_input.csv";
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

    private enum CsvDialect {
        RFC4180, EXCEL, EXCEL_SEMICOLON, TSV, JSON_CSV
    }

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

    private record CsvDialectResult(CsvDialect dialect, CsvFormat format) {}

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

    private static boolean askYesNo(Scanner sc, String prompt) {
        return prompt(sc, prompt).trim().equalsIgnoreCase("y");
    }

    private static String prompt(Scanner sc, String message) {
        System.out.print(message);
        return sc.nextLine().trim();
    }

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
     * Demonstrates a simple CSV round-trip:
     *  - read rows with CsvReader
     *  - display a preview in the console
     *  - write all rows back out with CsvWriter
     */
    private static void demoRoundTrip(Scanner sc) {
        System.out.print("Enter input CSV file path: ");
        String inPathStr = sc.nextLine().trim();
        if (inPathStr.isEmpty()) {
            System.out.println("No path entered, aborting.");
            return;
        }

        Path inPath = Paths.get(inPathStr);
        if (!Files.exists(inPath)) {
            System.out.println("Input file not found: " + inPath);
            return;
        }

        System.out.print("Enter output CSV file path (leave blank for demo_output.csv): ");
        String outPathStr = sc.nextLine().trim();
        if (outPathStr.isEmpty()) {
            outPathStr = "demo_output.csv";
        }
        Path outPath = Paths.get(outPathStr);

        // For demo purposes we use RFC-4180 with a header row.
        CsvConfig config = CsvConfig.builder()
                .setFormat(CsvFormat.rfc4180())
                .setHasHeader(true)
                .setRequireUniformFieldCount(true)
                .build();

        try (
                // Reader side: use CsvReader with the configured charset and format
                CsvReader reader = CsvReader.fromPath(inPath, config)
        ) {
            List<Row> rows = reader.readAll();
            System.out.printf("Read %d row(s) from %s%n", rows.size(), inPath);

            // Show a small preview in the console
            System.out.println("--- Preview (up to 5 rows) ---");
            rows.stream()
                    .limit(5)
                    .forEach(System.out::println);
            System.out.println("------------------------------");

            // Writer side: round-trip the same rows to the output file
            try (CsvWriter writer = CsvWriter.toPath(outPath, config)) {
                writer.writeAllRows(rows);
            }

            System.out.printf("Wrote %d row(s) to %s%n", rows.size(), outPath);
            System.out.println("Round-trip complete.");

        } catch (Exception e) {
            System.err.println("Error during round-trip: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

}
