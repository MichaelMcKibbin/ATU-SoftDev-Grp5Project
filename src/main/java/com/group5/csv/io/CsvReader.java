package com.group5.csv.io;

import com.group5.csv.core.Headers;
import com.group5.csv.core.Row;
import com.group5.csv.core.RowBuilder;
import com.group5.csv.exceptions.ParseException;
import static com.group5.csv.io.InputStreamDetector.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

//https://www.rfc-editor.org/rfc/rfc4180
//https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#lineending

//////This class can be called from main using code like the following
//        CsvReader csvReader = new CsvReader();
//        CsvConfig config = csvReader.createConfig();
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Please enter full CSV path and file name with its file extension:");
//        String fileName = scanner.nextLine();
//        fileName = fileName.replace("\\", "\\\\");
//        csvReader.fileToConsoleTable(fileName);

/**
 * A CSV reader capable of parsing delimited text using a configurable {@link CsvConfig}
 * and {@link CsvParser}. Supports optional header recognition, uniform column validation,
 * empty-line skipping, and iteration over parsed {@link Row} objects.
 * <p>
 * This class is stateful and not thread-safe. Each instance maintains its own
 * parser state, record number, and warnings collection.
 * </p>
 * <p>
 * Instances may be constructed from either an {@link InputStream} or a {@link Reader}.
 * Character decoding is controlled by the {@link CsvConfig#getCharset()} setting.
 * </p>
 */
public class CsvReader implements Closeable, Iterable<Row> {
    //Variables
    private final CsvConfig config;
    private final CsvParser parser;
    private final Reader in;
    private Headers headers;
//    private List<String> singleRow = new ArrayList<>();
//    private List<String> multiRow = new ArrayList<>();
    private int columnsCount = -1;
    private long recordNumber = 0;
    private CsvWarning lastRowWarning;


    // ----- getters -----

    /**
     * Returns the configuration used by this reader.
     *
     * @return the {@link CsvConfig} associated with this reader
     */
    public CsvConfig getConfig() { return config; }

    /**
     * Returns the underlying CSV parser.
     *
     * @return the {@link CsvParser} instance used to parse the input
     */
    public CsvParser getParser() { return parser; }

    /**
     * Returns the underlying {@link Reader} supplying input characters.
     *
     * @return the reader backing this CsvReader
     */
    public Reader getIn() { return in; }

    /**
     * Returns the parsed header definitions, if present.
     * <p>
     * If {@link CsvConfig#hasHeader()} is enabled, the first non-empty line is
     * interpreted as column headers unless explicitly provided at construction.
     * </p>
     *
     * @return the parsed {@link Headers}, or {@code null} if no headers are defined
     */
    public Headers getHeaders() { return headers; }

    /**
     * Returns the warning generated while parsing the most recently read row,
     * or {@code null} if the row was parsed without issues.
     *
     * @return the warning for the last row, or null if none
     */
    public CsvWarning getLastRowWarning() {
        return lastRowWarning;
    }


    // ----- constructors -----

    /**
     * Creates a new {@code CsvReader} using the given input stream and the default configuration.
     * The input stream is wrapped with an {@link InputStreamReader} using the default charset.
     *
     * @param input the input stream containing CSV data
     */
    public CsvReader(InputStream input) {
        this(input, createConfig());
    }

    /**
     * Creates a new {@code CsvReader} using the given reader and the default configuration.
     *
     * @param input the character reader providing CSV data
     */
    public CsvReader(Reader input) {
        this(input, createConfig());
    }

    /**
     * Creates a new {@code CsvReader} using the given input stream and configuration.
     * The input stream is decoded using the charset specified in the configuration.
     *
     * @param input  the input stream containing CSV data
     * @param config the reader configuration to apply
     */
    public CsvReader(InputStream input, CsvConfig config) {
        this(input, config, null);
    }

    /**
     * Creates a new {@code CsvReader} using the given reader and configuration.
     *
     * @param reader the character reader providing CSV data
     * @param config the reader configuration settings
     */
    public CsvReader(Reader reader, CsvConfig config) {
        this(reader, config, null);
    }

    /**
     * Creates a new {@code CsvReader} using the provided input stream, configuration,
     * and explicit headers. The stream is decoded with the charset defined in the configuration.
     *
     * @param input   the input stream containing CSV data
     * @param config  the reader configuration to apply
     * @param headers predefined column headers, or {@code null} to auto-detect if enabled
     */
    public CsvReader(InputStream input, CsvConfig config, Headers headers) {
        this(new InputStreamReader(input, config.getCharset()), config, headers);
    }

    /**
     * Creates a new {@code CsvReader} using the provided reader, configuration,
     * and optional explicit headers.
     *
     * @param reader  the character reader supplying CSV data
     * @param config  the reader configuration to use
     * @param headers predefined column headers, or {@code null} to auto-detect
     */
    public CsvReader(Reader reader, CsvConfig config, Headers headers) {
        this.in = reader instanceof BufferedReader ? reader :
                new BufferedReader(reader, config.getReadBufSize());
        this.config = config;
        this.headers = headers;
        if (headers != null)
            this.columnsCount = headers.size();
        this.parser = new CsvParser(config.getFormat(), in);
        this.lastRowWarning = null;
    }

    /**
     * Creates the default {@link CsvConfig} used when no configuration is supplied.
     * <p>
     * Defaults include: Excel-style format, UTF-8 charset, header enabled,
     * skipping empty lines, and an 8 KB read buffer.
     * </p>
     *
     * @return a newly created default configuration
     */
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

    /**
     * Creates a {@link CsvReader} from the given {@link Path} and configuration.
     * <p>
     * Charset detection is only performed when the configured charset belongs
     * to the UTF-8 family. If a different charset is configured, it is used as-is.
     * When detection occurs and a different charset is found, a new {@link CsvConfig}
     * is created via {@link CsvConfig#toBuilder()} with the detected charset while
     * preserving all other settings.
     *
     * @param path   the CSV file path; must not be {@code null}
     * @param config the CSV configuration; must not be {@code null}
     * @return a new {@link CsvReader} instance
     * @throws IllegalArgumentException if {@code path} or {@code config} is {@code null}
     * @throws IOException if an error occurs while opening the file
     */
    public static CsvReader fromPath(Path path, CsvConfig config) throws IOException
    {
        if (path == null) throw new IllegalArgumentException("path must not be null");
        if (config == null) throw new IllegalArgumentException("config must not be null");

        InputStreamDetector.Result detected = InputStreamDetector
                .detect(path.toFile(), config.getCharset());
        InputStream in = detected.stream;
        CsvConfig newConfig = detected.charset.equals(config.getCharset()) ?
                config : config.toBuilder().setCharset(detected.charset).build();
        return new CsvReader(in, newConfig);
    }

    /**
     * Creates a {@link CsvReader} for the given {@link Path} using the default configuration.
     *
     * @param path the CSV file path; must not be {@code null}
     * @return a new {@link CsvReader} instance
     * @throws IllegalArgumentException if {@code path} is {@code null}
     * @throws IOException if an error occurs while opening the file
     */
    public static CsvReader fromPath(Path path) throws IOException
    {
        return fromPath(path, createConfig());
    }


    // ----- behaviour methods -----

    /**
     * Reads all rows from the CSV input until end-of-file.
     * <p>
     * Empty rows are skipped if {@link CsvConfig#isSkipEmptyLines()} is enabled.
     * </p>
     *
     * @return a list of all parsed {@link Row} objects
     * @throws IOException if an I/O error occurs while reading
     */
    public List<Row> readAll() throws IOException {
        List<Row> result = new ArrayList<>();
        Row row = null;
        do {
            row = readRow();
            if (row != null) {
                // skip empty lines if asked to
                if (row.isEmpty() && config.isSkipEmptyLines())
                    continue;
                // consume row
                result.add(row);
            }
        } while (row != null);
        return result;
    }

    /**
     * Ensures that a row contains the expected number of fields, optionally adding
     * warnings and normalizing the field count based on the configuration.
     * <p>
     * If uniform field counts are required, rows that are shorter will be padded and
     * rows that are longer will be truncated. Warnings are recorded for mismatches.
     * </p>
     *
     * @param row the list of field values for the current CSV record
     */
    private void ensureCorrectFieldCount(List<String> row) {
        // Reset status for this row
        lastRowWarning = null;

        // value is undefined
        if (columnsCount < 0)
            return;

        // or ensuring not required
        if (! config.hasHeader() && ! config.isRequireUniformFieldCount())
            return;

        if (row.size() < columnsCount) {
            lastRowWarning = new CsvWarning(
                    recordNumber,
                    CsvWarning.Type.TOO_FEW_FIELDS,
                    "Row contains more fields than expected"
            );
            while (row.size() < columnsCount)
                row.add("");
        }

        if (row.size() > columnsCount) {
            lastRowWarning = new CsvWarning(
                    recordNumber,
                    CsvWarning.Type.TOO_MANY_FIELDS,
                    "Row contains more fields than expected"
            );
            while (row.size() > columnsCount)
                row.removeLast();
        }
    }

    /**
     * Reads and returns the next row of CSV data.
     * <p>
     * Header lines are processed automatically if enabled. Field counts are validated
     * or normalized depending on configuration. Parsing errors include line number
     * information and are rethrown as {@link ParseException}.
     * </p>
     *
     * @return the next parsed {@link Row}, or {@code null} at end-of-file
     * @throws IOException     if an I/O error occurs
     * @throws ParseException  if malformed CSV syntax is encountered
     */
    public Row readRow() throws IOException {
        // read one line from parser
        List<String> row;
        try {
            row = parser.readRow();
        } catch (ParseException ex) {
            ex.setLine(recordNumber);
            throw ex;
        }
        if (row == null)
            return null; // EOF

        // track line number
        ++recordNumber;

        // first line
        if (recordNumber == 1L) {
            // recognise headers if asked to
            if (config.hasHeader() && headers == null) {
                headers = new Headers(row);
                columnsCount = row.size();
                return readRow();

            // set columns count if uniform filed count is required
            } else if (config.isRequireUniformFieldCount()) {
                columnsCount = row.size();
            }
        }

        // verify column count is correct
        ensureCorrectFieldCount(row);

        // which headers?
        Headers rowHeaders = headers == null ? new Headers(row.size()) : headers;

        // return line transformed into Row type
        RowBuilder builder = new RowBuilder(rowHeaders);
        builder.addAll(row);
        return builder.build();
    }

    /**
     * Returns an iterator over the rows of this CSV reader.
     * <p>
     * The iterator reads rows lazily and wraps any {@link IOException}
     * in an {@link UncheckedIOException}. If no more rows are available,
     * {@link java.util.NoSuchElementException} is thrown.
     * </p>
     *
     * @return an iterator producing {@link Row} objects
     */
    @Override
    public Iterator<Row> iterator() {
        return new Iterator<>() {
            private Row nextRow;
            private boolean done = false;

            private void preload() {
                if (nextRow != null || done) return;
                try {
                    nextRow = readRow();
                    if (nextRow == null) done = true;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public boolean hasNext() {
                preload();
                return !done;
            }

            @Override
            public Row next() {
                preload();
                if (done) throw new NoSuchElementException();
                Row current = nextRow;
                nextRow = null;
                return current;
            }
        };
    }

    /**
     * Closes the underlying input reader.
     *
     * @throws IOException if an I/O error occurs while closing the reader
     */
    @Override
    public void close() throws IOException {
        in.close();
    }



//    private void setSingleRow(CsvParser csvParser) {
//        try {
//            singleRow = csvParser.readRow();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void fileToConsoleTable(String fileName) {
//        Reader reader = null;
//        try {
//            reader = new FileReader(fileName);
//            //Butch Cassidy
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        CsvConfig config = createConfig();
//        int i = 0;
//        int j = 0;
//        CsvParser parser = new CsvParser(config.getFormat(),reader);
//        setSingleRow(parser);
//        System.out.print(singleRow);
//        while (singleRow != null) {
//            for(j=0;j<singleRow.size();j++){
//                multiRow.add(singleRow.get(j));
//            }
//            setSingleRow(parser);
//        }
//        printTable(multiRow);
//        }
//
//    public static void printTable(List<String> cells) {
//        List<List<String>> rows = new ArrayList<>();
//        List<String> current = new ArrayList<>();
//
//        for (String cell : cells) {
//            if (cell.isEmpty()) {
//                if (!current.isEmpty()) {
//                    rows.add(current);
//                    current = new ArrayList<>();
//                }
//            } else {
//                current.add(cell);
//            }
//        }
//        if (!current.isEmpty()) {
//            rows.add(current);
//        }
//
//        // now print each row with the earlier method
//        for (List<String> row : rows) {
//            printTableRow(row);  // your previous method
//        }
//    }
//
//    public static void printTableRow(List<String> cells) {
//        int width = 15; // cell width
//
//        // top border
//        System.out.print("+");
//        for (int i = 0; i < cells.size(); i++) {
//            for (int j = 0; j < width; j++) {
//                System.out.print("-");
//            }
//            System.out.print("+");
//        }
//        System.out.println();
//
//        // row
//        System.out.print("|");
//        for (String cell : cells) {
//            String content = cell;
//            if (content.length() > width) {
//                content = content.substring(0, width);
//            }
//            System.out.print(content);
//            // pad with spaces
//            for (int j = content.length(); j < width; j++) {
//                System.out.print(" ");
//            }
//            System.out.print("|");
//        }
//        System.out.println();
//
//        // bottom border
//        System.out.print("+");
//        for (int i = 0; i < cells.size(); i++) {
//            for (int j = 0; j < width; j++) {
//                System.out.print("-");
//            }
//            System.out.print("+");
//        }
//        System.out.println();
//    }
    
}

