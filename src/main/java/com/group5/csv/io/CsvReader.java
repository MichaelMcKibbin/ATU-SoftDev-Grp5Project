package com.group5.csv.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Paths;
public class CsvReader {




    //https://www.rfc-editor.org/rfc/rfc4180
    //https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#lineending
    //Variables
    public String fullFileAddress;

    public String filePath;
    public String fileName;
    public String fileExtension;

    public ArrayList<String> rows = new ArrayList<String>();
    public CsvFormat csvFormat;
    String y;
    String w;



    //constructor
    public CsvReader() {
    }

    //readFileToRows with 5 parameters
    //Call readFileToRows with fiveParas = new readFileToRows(filePath, fileName,  fileExtension, rows, CsvFormat);
    public void readFileToRows(String filePath, String fileName, String fileExtension, ArrayList<String> rows, CsvFormat csvFormat) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.rows = rows;
        this.csvFormat = csvFormat;
        String fullFileAddress = filePath + "\\" + fileName + "." + fileExtension;
        readFileToRows(fullFileAddress,rows,csvFormat);
    }

    // readFileToRows with 3 parameters
    // Call readFileToRows with threeParas = new readFileToRows(fullFileAddress, rows, CsvFormat);
    // It is necessary to add double backslashes ("C:\\A\\B\\C\\D\\E.csv") except when using scanner user input where only single backslashes are needed.
    public void readFileToRows(String fullFileAddress, ArrayList rows, CsvFormat csvFormat) {
        this.fullFileAddress = fullFileAddress;
        this.rows = rows;
        this.csvFormat = csvFormat;
        String lineBreakerPattern = (csvFormat.printableLineSeparator(csvFormat.newline) != null ? csvFormat.printableLineSeparator(csvFormat.newline).replace("\\","\\\\" ):"");

        try {
            makeAndRead(fullFileAddress, rows,lineBreakerPattern);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Read file text to array list
    public void makeAndRead(String y, ArrayList<String> d, String w) throws FileNotFoundException {
        this.y = y;
        File x = new File(y);
        this.w = w;
        System.out.println(w);

        try (Scanner z = new Scanner(x)) {
            while (z.hasNextLine()) {
                String data = z.nextLine();

                Pattern w1 = Pattern.compile(w);
                Matcher m = w1.matcher(data);
                ArrayList<Integer> matchStarts = new ArrayList<>();
                ArrayList<Integer> matchEnds = new ArrayList<>();

                while (m.find()) {
                    // Check if this match is inside quotes
                    if (!isInsideQuotes(data, m.start())) {
                        matchStarts.add(m.start());
                        matchEnds.add(m.end());
                    }
                }

                if (matchStarts.isEmpty()) {
                    d.add(data);
                } else {
                    d.add(data.substring(0, matchStarts.get(0)));

                    if (matchStarts.size() > 1) {
                        for (int i = 0; i < matchStarts.size() - 1; i++) {
                            d.add(data.substring(matchEnds.get(i), matchStarts.get(i + 1)));
                        }
                        d.add(data.substring(matchEnds.get(matchStarts.size() - 1), data.length()));
                    } else {
                        d.add(data.substring(matchEnds.get(0)));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // Helper method to check if a position is inside quotes
    private boolean isInsideQuotes(String line, int position) {
        int quoteCount = 0;
        for (int i = 0; i < position; i++) {
            if (line.charAt(i) == '"') {
                // Check if quote is escaped
                if (i > 0 && line.charAt(i - 1) == '\\') {
                    continue; // Skip escaped quotes
                }
                quoteCount++;
            }
        }
        // If odd number of quotes before position, we're inside quotes
        return quoteCount % 2 == 1;
    }
}

