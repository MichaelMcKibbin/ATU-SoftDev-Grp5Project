package com.group5.csv.io;

import com.group5.csv.core.Headers;
import com.group5.csv.core.Row;
import com.group5.csv.core.RowBuilder;
import com.group5.csv.exceptions.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
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

public class CsvReader implements Closeable, Iterable<Row> {

    private final CsvConfig config;
    private final CsvParser parser;
    private final Reader in;
    private Headers headers;
    private final List<String> warnings;
    private List<String> singleRow = new ArrayList<>();
    private List<String> multiRow = new ArrayList<>();

    public CsvConfig getConfig() { return config; }
    public CsvParser getParser() { return parser; }
    public Reader getIn() { return in; }
    public Headers getHeaders() { return headers; }
    public List<String> getWarnings() { return Collections.unmodifiableList(warnings); }

    //https://www.rfc-editor.org/rfc/rfc4180
    //https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#lineending
    //Variables
    //constructor

    public CsvReader(InputStream input) {
        this(input, createConfig());
    }

    public CsvReader(Reader input) {
        this(input, createConfig());
    }

    public CsvReader(InputStream input, CsvConfig config) {
        this(input, config, null);
    }

    public CsvReader(Reader reader, CsvConfig config) {
        this(reader, config, null);
    }

    public CsvReader(InputStream input, CsvConfig config, Headers headers) {
        this(new InputStreamReader(input, config.getCharset()), config, headers);
    }

    public CsvReader(Reader reader, CsvConfig config, Headers headers) {
        this.in = reader instanceof BufferedReader ? reader :
                new BufferedReader(reader, config.getReadBufSize());
        this.config = config;
        this.headers = headers;
        this.parser = new CsvParser(config.getFormat(), in);
        this.warnings = new ArrayList<>();
    }

    private static CsvConfig createConfig() {
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

    public List<Row> readAll() throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Row readRow() throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Iterator<Row> iterator() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void close() throws IOException {
        in.close();
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

