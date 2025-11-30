package com.group5.csv.demo;

import com.group5.csv.io.*;
import com.group5.csv.core.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
                case "1" -> demoRead(sc);
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


    // try these when CsvReader & CsvWriter are completed
    private static void demoRead(Scanner sc) {
        try {
            System.out.print("Enter CSV file path: ");
            String path = sc.nextLine();
            File file = new File(path);

            var result = InputStreamDetector.detect(file, StandardCharsets.UTF_8);
            CsvReader reader = new CsvReader(result.stream, new CsvConfig.Builder().setCharset(result.charset).build());

            List<Row> rows = reader.readAll();
            System.out.printf("Read %d rows.%n", rows.size());
            rows.stream().limit(5).forEach(System.out::println); // show first 5 rows
        } catch (Exception e) {
            System.err.println("Error reading CSV: " + e.getMessage());
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
                CsvReader reader = new CsvReader(
                        Files.newBufferedReader(inPath, config.getCharset()),
                        config
                )
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
