package com.group5.csv.io;//package com.example.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.IntStream;

public class CsvStream {

    public static void main(String[] args) {
        String fileToWrite = "output.csv";

        try {
            // write the CSV file line by line
            writeCsvStream(fileToWrite);

            // read the CSV file line by line
            readCsvStream(fileToWrite);

        } catch (IOException ex) {
            // print any error that occurs
            System.err.println("Error: " + ex.getMessage());
        }
    }

    public static void writeCsvStream(String filePath) throws IOException {
        // BufferedWriter writes text efficiently
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("id,name,email");
            writer.newLine();

            IntStream.rangeClosed(1, 1000).forEach(i -> {
                try {
                    String line = i + ",User_" + i + ",user" + i + "@mail.com";
                    writer.write(line);
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static void readCsvStream(String filePath) throws IOException {
        // BufferedReader reads text line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            reader.lines()
                    .skip(1)
                    .map(line -> line.split(","))
                    .forEach(columns -> {
                        if (columns.length >= 3) {
                            String id = columns[0];
                            String name = columns[1];
                            String email = columns[2];

                            System.out.println("ID: " + id +
                                    " | Name: " + name +
                                    " | Email: " + email);
                        }
                    });
        }
    }
}
//