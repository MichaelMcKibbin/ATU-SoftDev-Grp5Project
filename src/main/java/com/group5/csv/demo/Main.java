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
            System.out.println("0) Exit");
            System.out.print("Choose option: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> demoRead(sc);
                case "2" -> demoWrite();
                case "3" -> System.out.println("Schema validation not yet implemented.");
                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    /** Placeholder: previews the file without using CsvReader (keeps demo usable). */
    private static void demoRead(Scanner sc) {
        System.out.print("Enter CSV file path: ");
        String path = sc.nextLine().trim();
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            System.out.println("File not found. (CsvReader not fully implemented yet.)");
            return;
        }
        try {
            List<String> lines = Files.readAllLines(p); // placeholder
            System.out.printf("Read %d line(s). Showing up to 5:%n", lines.size());
            lines.stream().limit(5).forEach(System.out::println);
            System.out.println("\nNote: This is a placeholder preview. Full CsvReader integration pending. This message created in Main");
        } catch (IOException e) {
            System.out.println("Could not read file (placeholder mode): " + e.getMessage());
        }
    }

    /** Placeholder: writes a minimal CSV using NIO (no CsvWriter yet). */
    private static void demoWrite() {
        Path out = Paths.get("demo_output.csv");
        try {
            Files.write(out, List.of("id,name,age", "1,John,20", "2,Bob,30"));
            System.out.println("Created demo_output.csv (placeholder). CsvWriter integration pending.");
        } catch (IOException e) {
            System.out.println("Could not write demo_output.csv (placeholder mode): " + e.getMessage());
        }
    }


//    // try these when CsvReader & CsvWriter are completed
//    private static void demoRead(Scanner sc) {
//        try {
//            System.out.print("Enter CSV file path: ");
//            String path = sc.nextLine();
//            File file = new File(path);
//
//            var result = InputStreamDetector.detect(file, StandardCharsets.UTF_8);
//            CsvReader reader = new CsvReader(result.stream, result.charset);
//
//            List<Row> rows = reader.readAll();
//            System.out.printf("Read %d rows.%n", rows.size());
//            rows.stream().limit(5).forEach(System.out::println); // show first 5 rows
//        } catch (Exception e) {
//            System.err.println("Error reading CSV: " + e.getMessage());
//        }
//    }
//
//    private static void demoWrite() {
//        try {
//            File file = new File("demo_output.csv");
//            CsvConfig config = new CsvConfig();
//            CsvWriter writer = new CsvWriter(file, config);
//
//            writer.writeRow(new String[]{"id", "name", "age"});
//            writer.writeRow(new String[]{"1", "Alice", "25"});
//            writer.writeRow(new String[]{"2", "Bob", "30"});
//            writer.close();
//
//            System.out.println("Created demo_output.csv successfully.");
//        } catch (Exception e) {
//            System.err.println("Error writing CSV: " + e.getMessage());
//        }
//    }
}
