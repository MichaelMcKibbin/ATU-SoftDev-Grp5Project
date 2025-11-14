package com.group5.csv.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvReader {

    //https://www.rfc-editor.org/rfc/rfc4180
    //https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#lineending
    //Variables
    public String fullFileAddress;

    public String filePath;
    public String fileName;
    public String fileExtension;

    public ArrayList<String> rows = new ArrayList<String>();
    String y;

    //Constructor with 4 parameters
    //Call with CsvReader fourParas = new CsvReader(filePath, fileName,  fileExtension, rows);
    public CsvReader(String filePath, String fileName, String fileExtension, ArrayList<String> rows) {

        this.filePath = filePath;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.rows = rows;
        String fullFileAddress = filePath + "\\" + fileName + "." + fileExtension;
        try {
            makeAndRead(fullFileAddress, rows);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Constructor with 2 parameters
    // Call with CsvReader twoParas = new CsvReader(fullFileAddress, rows);
    // It is necessary to add double backslashes ("C:\\A\\B\\C\\D\\E.csv") except when using scanner user input where only single backslashes are needed.
    public CsvReader(String fullFileAddress, ArrayList rows) {
        this.fullFileAddress = fullFileAddress;
        this.rows = rows;

        try {
            makeAndRead(fullFileAddress, rows);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Read file text to array list
    public void makeAndRead(String y, ArrayList d) throws FileNotFoundException {
        this.y = y;
        File x = new File(y);

        try (Scanner z = new Scanner(x)) {
            while (z.hasNextLine()) {
                String data = z.nextLine();
                Pattern anyLineBreak = Pattern.compile("(?s)\\\\r\\\\n|\\\\r|\\\\n");
                String regex1 = "(?s)(\\\\r\\\\n|\\\\r|\\\\n)(?=(?:[^\\\\\\\"]*\\\\\\\"[^\\\\\\\"]*\\\\\\\")*[^\\\\\\\"]*$)";
                Pattern ALBNotInDoubleQuotes = Pattern.compile(regex1);
                Matcher m = ALBNotInDoubleQuotes.matcher(data);
                ArrayList<Integer> matchStarts =  new ArrayList<>();
                ArrayList<Integer> matchEnds =  new ArrayList<>();
                if (m.find()) {
                    matchStarts.add(m.start());
                    matchEnds.add(m.end());
                    while (m.find()) {
                        matchStarts.add(m.start());
                        matchEnds.add(m.end());
                    }
                    //for testing only
//                    d.add(data);
                    d.add(data.substring(0,matchStarts.get(0)));
                    if (matchStarts.size() > 1) {

                        for (int i = 0; i < matchStarts.size() - 1; i++) {
                            d.add(data.substring(matchEnds.get(i), matchStarts.get(i + 1)));
                        }
//                    after last match
                        d.add(data.substring(matchEnds.get(matchStarts.size() - 1), data.length()));

                    } else {
                        d.add(data.substring(matchEnds.get(0)));

                    }

//                        d.add(data.substring(matchEnds.get(matchEnds.size() - 1)));
                } else {
                    //else if no match
                    d.add(data);
                }

            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
