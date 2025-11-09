import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class CsvReader {

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
                d.add(data);
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

