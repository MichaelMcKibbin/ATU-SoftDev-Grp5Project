package com.group5.csv.io;

import com.group5.csv.exceptions.ParseException;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *      CsvParser
 * ----------------------------------------------------------------------------
 * Reads Rows from Reader in form of List<String>
 * Complies with CsvFormat settings
 *
 * */
class CsvParser {
    private static final int EOF = -1;
    private static final char EOL = '\n';

    private final CsvFormat format;
    private final PushbackReader reader;


    private enum State {
        StartRow,
        StartCell,
        EndCell,
        InsideQuotedCell,
        InsideUnquotedCell,
        NextToQuote,
        EndRow,
    }

    State state;
    List<String> row;
    StringBuilder cell;

    public CsvParser(CsvFormat format, Reader reader) {
        this.format = Objects.requireNonNull(format);
        if (!(reader instanceof PushbackReader)) {
            // wrap in PushbackReader for char pushback
            this.reader = new PushbackReader(reader, 2);
        } else {
            this.reader = (PushbackReader) reader;
        }
    }

    // helper methods
    private void clean() { row = null; cell = null; }
    private void newRow() { row = new ArrayList<>(); }
    private void newCell() { cell = new StringBuilder(); }
    private void saveCell() { row.add(cell.toString()); }
    private void consume(int ch) { cell.append((char)ch); }

    /** transition between Finate State Machine states */
    private void transition(int ch) throws IOException
    {
        switch (state) {

            case StartRow -> {
                if (ch == EOF)
                    state = State.EndRow;
                else { // Any
                    reader.unread(ch);
                    newRow();
                    state = State.StartCell;
                }

            } case StartCell -> {
                newCell();
                if (ch == EOF || ch == EOL || ch == format.delimiter) {
                    reader.unread(ch);
                    state = State.EndCell;
                } else if (ch == format.quoteChar) {
                    state = State.InsideQuotedCell;
                } else { // Any
                    consume(ch);
                    state = State.InsideUnquotedCell;
                }

            } case InsideQuotedCell -> {
                if (ch == EOF) {
                    throw new ParseException("CsvParser: Unexpected EOF!");
                } else if (ch == format.quoteChar) {
                    state = State.NextToQuote;
                } else { // Any
                    consume(ch);
                }

            } case InsideUnquotedCell -> {
                if (ch == format.quoteChar) {
                    throw new ParseException("CsvParser: Unexpected Quote Symbol!");
                } else if (ch == EOF || ch == EOL || ch == format.delimiter) {
                    reader.unread(ch);
                    state = State.EndCell;
                } else { // Any
                    consume(ch);
                }

            } case NextToQuote -> {
                if (ch == format.quoteChar) {
                    consume(ch);
                    state = State.InsideQuotedCell;
                } else if (ch == EOF || ch == EOL || ch == format.delimiter) {
                    reader.unread(ch);
                    state = State.EndCell;
                } else { // Any
                    throw new ParseException("CsvParser: Unexpected Character After Quote!");
                }

            } case EndCell -> {
                saveCell();
                if (ch == format.delimiter) {
                    state = State.StartCell;
                } else { // ch == EOF || ch == EOL
                    state = State.EndRow;
                }

            } default -> {
                throw new ParseException("CsvParser: State Unknown!");
            }
        }
    }

    // parse a row
    public List<String> readRow() throws IOException, ParseException {
        // initialise
        state = State.StartRow;
        clean();

        // parse row from input stream
        int ch;
        do {
            // read char by char from `reader`
            ch = reader.read();

            // handle different new line formats
            if (ch == '\r') {
                int nextCh = reader.read();
                if (nextCh != EOL)
                    reader.unread(nextCh);
                ch = EOL;
            }

            // transition to next state
            transition(ch);

        // exit condition: state == State.EndRow && (ch == EOL || ch == EOF)
        } while ((ch != EOF && ch != EOL) || state != State.EndRow);

        // row parsing complete
        return row; // saveRow()
    }
}
