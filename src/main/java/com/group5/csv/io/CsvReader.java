package com.group5.csv.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Paths;

////This class can be called from main using code like the following
/**
        CsvReader csvReader = new CsvReader();
        CsvConfig config = csvReader.createConfig();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter full CSV path and file name with its file extension:");
        String fileName = scanner.nextLine();
        fileName = fileName.replace("\\", "\\\\");
        csvReader.fileToConsoleTable(fileName);
 */

public class CsvReader {

private List<String> singleRow = new ArrayList<>();
private List<String> multiRow = new ArrayList<>();


    //https://www.rfc-editor.org/rfc/rfc4180
    //https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#lineending
    //Variables
    //constructor

    public CsvReader() {
    }

    public CsvConfig createConfig() {
        CsvConfig config = new CsvConfig.Builder()
                .setFormat(CsvFormat.excel())
                .setHasHeader(true)
                .setRequireUniformFieldCount(false)
                .setSkipEmptyLines(true)
                .setCharset(StandardCharsets.UTF_8)
                .setWriteBOM(true)
                .setReadBufSize(8192)
                .build();

        return config;


    }

    private void setSingleRow(CsvParser csvParser) {
        try {
            singleRow = csvParser.readRow();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fileToConsoleTable(String fileName) {
        Reader reader = null;
        try {
            reader = new FileReader(fileName);
            //Butch Cassidy
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        CsvConfig config = createConfig();
        int i = 0;
        int j = 0;
        CsvParser parser = new CsvParser(config.getFormat(),reader);
        setSingleRow(parser);
        System.out.print(singleRow);
        while (singleRow != null) {
            for(j=0;j<singleRow.size();j++){
                multiRow.add(singleRow.get(j));
            }
            setSingleRow(parser);
        }
        printTable(multiRow);
        }

    public static void printTable(List<String> cells) {
        List<List<String>> rows = new ArrayList<>();
        List<String> current = new ArrayList<>();

        for (String cell : cells) {
            if (cell.isEmpty()) {
                if (!current.isEmpty()) {
                    rows.add(current);
                    current = new ArrayList<>();
                }
            } else {
                current.add(cell);
            }
        }
        if (!current.isEmpty()) {
            rows.add(current);
        }

        // now print each row with the earlier method
        for (List<String> row : rows) {
            printTableRow(row);  // your previous method
        }
    }

    public static void printTableRow(List<String> cells) {
        int width = 15; // cell width

        // top border
        System.out.print("+");
        for (int i = 0; i < cells.size(); i++) {
            for (int j = 0; j < width; j++) {
                System.out.print("-");
            }
            System.out.print("+");
        }
        System.out.println();

        // row
        System.out.print("|");
        for (String cell : cells) {
            String content = cell;
            if (content.length() > width) {
                content = content.substring(0, width);
            }
            System.out.print(content);
            // pad with spaces
            for (int j = content.length(); j < width; j++) {
                System.out.print(" ");
            }
            System.out.print("|");
        }
        System.out.println();

        // bottom border
        System.out.print("+");
        for (int i = 0; i < cells.size(); i++) {
            for (int j = 0; j < width; j++) {
                System.out.print("-");
            }
            System.out.print("+");
        }
        System.out.println();
    }
    
}

