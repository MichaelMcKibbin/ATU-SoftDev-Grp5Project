package com.group5.csv.io;

import com.group5.csv.exceptions.ParseException;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Low-level CSV parser that reads characters from a Reader and
 * returns one logical record at a time as a list of fields.
 *
 * This is a state machine that honours:
 *  - delimiter and quoteChar from CsvFormat
 *  - CRLF vs LF newlines (CRLF normalised to a single end-of-row)
 *
 * It does NOT handle higher-level concerns like headers or schemas.
 */
public final class CsvParser {

    private final CsvFormat format;
    private final PushbackReader in;

    // Internal FSM states
    private enum State {
        START_ROW,
        START_CELL,
        INSIDE_QUOTED,
        INSIDE_UNQUOTED,
        AFTER_QUOTE
    }

    public CsvParser(CsvFormat format, Reader reader) {
        if (format == null) throw new IllegalArgumentException("format must not be null");
        if (reader == null) throw new IllegalArgumentException("reader must not be null");
        this.format = format;
        this.in = new PushbackReader(reader, 2);
    }

    public CsvFormat getFormat() { return format; }

    /**
     * Reads the next logical row from the input.
     *
     * @return list of cell values, or null if EOF is reached before any data
     * @throws IOException if the underlying reader fails
     * @throws ParseException (runtime) if CSV syntax is invalid
     */
    public List<String> readRow() throws IOException {
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();

        State state = State.START_ROW;
        int chInt;

        while (true) {
            chInt = in.read();
            char ch = (char) chInt;

            if (chInt == -1) { // EOF
                switch (state) {
                    case START_ROW -> {
                        // EOF before any data
                        return row.isEmpty() ? null : row;
                    }
                    case INSIDE_QUOTED/*, AFTER_QUOTE*/ ->
                            throw new ParseException("Unexpected end of file inside quoted field");
                    default -> {
                        // push last cell if there is one
                        row.add(cell.toString());
                        return row;
                    }
                }
            }

            // normalise CRLF => treat \r\n as single newline
            if (ch == '\r') {
                int next = in.read();
                if (next != '\n' && next != -1) {
                    in.unread(next);
                }
                ch = '\n';
            }

            switch (state) {
                case START_ROW -> {
                    in.unread(ch);
                    state = State.START_CELL;
//                    // first character of a new row
//                    if (ch == '\n') {
//                        // empty line -> return single empty cell
//                        row.add("");
//                        return row;
//                    } else if (ch == format.quoteChar) {
//                        state = State.INSIDE_QUOTED;
//                        if (format.skipWhitespaceAroundQuotes) {
//                            cell.setLength(0); // skip tabs and spaces in excel-like syntax
//                        }
//                    } else if (ch == format.delimiter) {
//                        row.add("");
//                        state = State.START_CELL;
//                    } else if ((ch == ' ' || ch == '\t') && format.skipWhitespaceAroundQuotes) {
//                        cell.append(ch);
//                        // collect tabs and spaces in excel-like syntax
//                        // in order to skip them if field is quoted (when quote symbol encountered)
//                        // or retain otherwise
//                    } else {
//                        cell.append(ch);
//                        state = State.INSIDE_UNQUOTED;
//                    }
                }

                case START_CELL -> {
                    if (ch == '\n') {
                        row.add("");
                        return row;
                    } else if (ch == format.quoteChar) {
                        state = State.INSIDE_QUOTED;
                        if (format.skipWhitespaceAroundQuotes) {
                            cell.setLength(0); // skip tabs and spaces in excel-like syntax
                        }
                    } else if (ch == format.delimiter) {
                        row.add("");
                        // stay in START_CELL for next empty cell
                    } else if ((ch == ' ' || ch == '\t') && format.skipWhitespaceAroundQuotes) {
                        cell.append(ch);
                        // collect tabs and spaces in excel-like syntax
                        // in order to skip them if field is quoted (when quote symbol encountered)
                        // or retain otherwise
                    } else {
                        cell.append(ch);
                        state = State.INSIDE_UNQUOTED;
                    }
                }

                case INSIDE_QUOTED -> {
                    if (ch == format.quoteChar) {
                        // Possible end of quoted field or escaped quote
                        int next = in.read();
                        if (next == format.quoteChar && format.doubleQuoteEnabled) {
                            // Escaped quote ("")
                            cell.append(format.quoteChar);
                        } else {
                            if (next != -1) {
                                in.unread(next);
                            }
                            state = State.AFTER_QUOTE;
                        }
                    } else {
                        cell.append(ch);
                    }
                }

                case AFTER_QUOTE -> {
                    if (ch == format.delimiter) {
                        row.add(cell.toString());
                        cell.setLength(0);
                        state = State.START_CELL;
                    } else if (ch == '\n') {
                        row.add(cell.toString());
                        return row;
                    } else if (format.skipWhitespaceAroundQuotes && (ch == '\t' || ch == ' ')) {
                        // Excel-style: ignore trailing spaces and tabs after closing quote
                        // and before delimiter or newline
                        // Just stay in AFTER_QUOTE
                    } else {
                        // non-whitespace garbage after closing quote
                        throw new ParseException("Unexpected character '" + ch +
                                "' after closing quote");
//                        if (!format.allowUnescapedQuotes) {
//                            throw new ParseException("Unexpected character '" + ch +
//                                    "' after closing quote");
//                        } else {
//                            // lenient mode: treat as normal char
//                            cell.append(ch);
//                            state = State.INSIDE_UNQUOTED;
//                        }
                    }
                }

                case INSIDE_UNQUOTED -> {
                    if (ch == format.delimiter) {
                        String value = cell.toString();
                        if (format.trimUnquotedFields) value = value.trim();
                        row.add(value);
                        cell.setLength(0);
                        state = State.START_CELL;
                    } else if (ch == '\n') {
                        String value = cell.toString();
                        if (format.trimUnquotedFields) value = value.trim();
                        row.add(value);
                        return row;
                    } else if (ch == format.quoteChar && !format.allowUnescapedQuotes) {
                        throw new ParseException("Unexpected quote in unquoted field");
                    } else {
                        cell.append(ch);
                    }
                }
            }
        }
    }
}


//package com.group5.csv.io;
//
//import com.group5.csv.exceptions.ParseException;
//
//import java.io.IOException;
//import java.io.PushbackReader;
//import java.io.Reader;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
///**
// *      CsvParser
// * ----------------------------------------------------------------------------
// * Reads Rows from Reader in form of List<String>
// * Complies with CsvFormat settings
// *
// * */
//class CsvParser {
//    private static final int EOF = -1;
//    private static final char EOL = '\n';
//
//    private final CsvFormat format;
//    private final PushbackReader reader;
//
//
//    private enum State {
//        StartRow,
//        StartCell,
//        EndCell,
//        InsideQuotedCell,
//        InsideUnquotedCell,
//        NextToQuote,
//        EndRow,
//    }
//
//    State state;
//    List<String> row;
//    StringBuilder cell;
//
//    public CsvParser(CsvFormat format, Reader reader) {
//        this.format = Objects.requireNonNull(format);
//        if (!(reader instanceof PushbackReader)) {
//            // wrap in PushbackReader for char pushback
//            this.reader = new PushbackReader(reader, 2);
//        } else {
//            this.reader = (PushbackReader) reader;
//        }
//    }
//
//    // helper methods
//    private void clean() { row = null; cell = null; }
//    private void newRow() { row = new ArrayList<>(); }
//    private void newCell() { cell = new StringBuilder(); }
//    private void saveCell() { row.add(cell.toString()); }
//    private void consume(int ch) { cell.append((char)ch); }
//
//    /** transition between Finite State Machine states */
//    private void transition(int ch) throws IOException
//    {
//        switch (state) {
//
//            case StartRow -> {
//                if (ch == EOF)
//                    state = State.EndRow;
//                else { // Any
//                    reader.unread(ch);
//                    newRow();
//                    state = State.StartCell;
//                }
//
//            } case StartCell -> {
//                newCell();
//                if (ch == EOF || ch == EOL || ch == format.delimiter) {
//                    reader.unread(ch);
//                    state = State.EndCell;
//                } else if (ch == format.quoteChar) {
//                    state = State.InsideQuotedCell;
//                } else { // Any
//                    consume(ch);
//                    state = State.InsideUnquotedCell;
//                }
//
//            } case InsideQuotedCell -> {
//                if (ch == EOF) {
//                    throw new ParseException("CsvParser: Unexpected EOF!");
//                } else if (ch == format.quoteChar) {
//                    state = State.NextToQuote;
//                } else { // Any
//                    consume(ch);
//                }
//
//            } case InsideUnquotedCell -> {
//                if (ch == format.quoteChar) {
//                    throw new ParseException("CsvParser: Unexpected Quote Symbol!");
//                } else if (ch == EOF || ch == EOL || ch == format.delimiter) {
//                    reader.unread(ch);
//                    state = State.EndCell;
//                } else { // Any
//                    consume(ch);
//                }
//
//            } case NextToQuote -> {
//                if (ch == format.quoteChar) {
//                    consume(ch);
//                    state = State.InsideQuotedCell;
//                } else if (ch == EOF || ch == EOL || ch == format.delimiter) {
//                    reader.unread(ch);
//                    state = State.EndCell;
//                } else { // Any
//                    throw new ParseException("CsvParser: Unexpected Character After Quote!");
//                }
//
//            } case EndCell -> {
//                saveCell();
//                if (ch == format.delimiter) {
//                    state = State.StartCell;
//                } else { // ch == EOF || ch == EOL
//                    state = State.EndRow;
//                }
//
//            } default -> {
//                throw new ParseException("CsvParser: State Unknown!");
//            }
//        }
//    }
//
//    // parse a row
//    public List<String> readRow() throws IOException, ParseException {
//        // initialise
//        state = State.StartRow;
//        clean();
//
//        // parse row from input stream
//        int ch;
//        do {
//            // read char by char from `reader`
//            ch = reader.read();
//
//            // handle different new line formats
//            if (ch == '\r') {
//                int nextCh = reader.read();
//                if (nextCh != EOL)
//                    reader.unread(nextCh);
//                ch = EOL;
//            }
//
//            // transition to next state
//            transition(ch);
//
//        // exit condition: state == State.EndRow && (ch == EOL || ch == EOF)
//        } while ((ch != EOF && ch != EOL) || state != State.EndRow);
//
//        // row parsing complete
//        return row; // saveRow()
//    }
//}
