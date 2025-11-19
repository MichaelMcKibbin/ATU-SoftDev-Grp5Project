# How should the CsvReader Function
This is a general guide, and we may not do everything exactly as laid out here. 
So long as we achieve the core functionality required, the optional extras can wait.

At a high level, CsvReader should be the coordinator that sits on top of:
- the finite state machine (character-level CSV parser),
- RowBuilder + Headers (record-level construction),
- and later, FieldType / schema stuff (typed values & validation).

It should turn a byte stream into a stream of Row objects in a safe, predictable way.

________________________________________
## Responsibilities of CsvReader
CsvReader should:

1.	**Own the input source**
- Wrap an InputStream or Reader in a BufferedReader.
- Or may be created via InputStreamDetector (BOM / charset detection).
2.	**Use the CSV dialect**
- Knows about CsvFormat (delimiter, quote char, newline, etc.).
- Passes these rules to the FSM / parser.
3.	**Handle headers**
- Optionally read the first record as headers:
- Produce a Headers object.
- Or accept a Headers supplied by the caller.
- Or operate “headerless” and generate default names (col0, col1, …).
4.	**Loop over records**
- Use the FSM to turn characters into List<String> cells.
- For each record:
- Use RowBuilder to build a Row.
- Return rows one-by-one (iterator/stream) or as a list (readAll).
5.	**Handle errors & options**
- Decide what to do on malformed CSV:
- strict mode → throw exception.
- lenient mode → collect errors, maybe skip bad rows.
- Optionally skip blank lines, comment lines, etc. (if in spec).
6.	**Be easy to test**
- No hard-wired System.in / System.out.
- Purely pull data from a Reader.
________________________________________
## An (incomplete) example of a CsvReader API  
Something along these lines:
```java
Package and import statements here.
Import our Headers, Row, RowBuilder, CsvFormat; 
Plus whatever java language imports are required – the IDE will likely autofill these.

public final class CsvReader implements Closeable, Iterable<Row> {

    private final BufferedReader in;
    private final CsvFormat format;
    private Headers headers;
    private final boolean firstRowAsHeader;

    public CsvReader(InputStream input, Charset charset, CsvFormat format, boolean firstRowAsHeader) {
        this(new InputStreamReader(input, charset), format, firstRowAsHeader);
    }

    public CsvReader(Reader reader, CsvFormat format, boolean firstRowAsHeader) {
        this.in = new BufferedReader(reader);
        this.format = format;
        this.firstRowAsHeader = firstRowAsHeader;
    }

    /** For externally supplied headers. Optional. */
    public CsvReader(Reader reader, CsvFormat format, Headers headers) {
        this.in = new BufferedReader(reader);
        this.format = format;
        this.firstRowAsHeader = false;
        this.headers = headers;
    }

    public Headers getHeaders() throws IOException {
        ensureHeadersLoaded();
        return headers;
    }

    public List<Row> readAll() throws IOException {
        ensureHeadersLoaded();
        List<Row> rows = new ArrayList<>();
        Row row;
        while ((row = readRow()) != null) {
            rows.add(row);
        }
        return rows;
    }

    /**
     * Reads the next Row or returns null at EOF.
     */
    public Row readRow() throws IOException {
        ensureHeadersLoaded();

        // Use FSM-based parser here to get fields:
        List<String> cells = parseNextRecord();
        if (cells == null) {
            return null; // EOF
        }

        RowBuilder builder = new RowBuilder(headers);
        builder.addAll(cells);
        return builder.build();
    }

    @Override
    public Iterator<Row> iterator() {
        // Can be implemented via a RowIterator that calls readRow()
        throw new UnsupportedOperationException("iterator() not implemented yet");
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    // Internals
    private void ensureHeadersLoaded() throws IOException {
        if (headers != null) return;

        List<String> firstRecord = parseNextRecord();
        if (firstRecord == null) {
            headers = new Headers(List.of()); // empty file
            return;
        }

        if (firstRowAsHeader) {
            headers = new Headers(firstRecord);
        } else {
            // no header mode: generate default names col0, col1, ...
            List<String> names = new ArrayList<>();
            for (int i = 0; i < firstRecord.size(); i++) {
                names.add("col" + i);
            }
            headers = new Headers(names);

            // And treat firstRecord as a data row
            RowBuilder builder = new RowBuilder(headers);
            builder.addAll(firstRecord);
            // this could be stored as a "pending first row" for readRow()/readAll()
        }
    }

    /**
     * Core FSM-based parsing logic should be here.
     * Returns a list of cell values for the next record, 
     * or null on EOF.
     */
    private List<String> parseNextRecord() throws IOException {

// Pseudocode…
//real implementation will use the finite state machine.
// - read chars
// - track states: OUTSIDE_FIELD, IN_UNQUOTED_FIELD, IN_QUOTED_FIELD, AFTER_QUOTE
// - fill a List<String> for one record
// - return that list, or null if EOF before any data

        return null;
    }
}
```

We don’t have to implement all of that at once, or right away, but this gives us:
•	responsibilities,
•	testable units,
•	and a nice document for our report.
________________________________________

## Where the FSM fits
The FSM lives inside parseNextRecord().

Rough flow:  BufferedReader → chars → FSM → List<String> cells → RowBuilder → Row

Pseudo-steps inside parseNextRecord():
**1.	Initialise:**
- state = OUTSIDE_FIELD
- currentField = new StringBuilder()
- fields = new ArrayList<String>()

**2.	Read characters one by one:**
- Switch on state and char:
- OUTSIDE_FIELD:
- delimiter → empty field
- quote → go to IN_QUOTED_FIELD
- newline/EOF → empty record / end
- other → start IN_UNQUOTED_FIELD
- IN_UNQUOTED_FIELD:
- delimiter → finish field
- newline → finish field & record
- else → append char
- IN_QUOTED_FIELD:
- quote → maybe go to AFTER_QUOTE (or handle escaped quotes)
- else → append char
- AFTER_QUOTE:
- delimiter → finish field
- newline → finish field & record
- EOF → finish field & record
- anything else → malformed

**3.	When record ends:**
- add last currentField.toString() to fields
- return fields

**4.	On EOF before any field:**
- return null. (format gives us the delimiter, quote char, newline rules, etc.)
________________________________________
## How CsvReader ties in with the rest of the core

**CsvReader**
•	owns the Reader and the FSM,
•	knows the CsvFormat,
•	manages Headers startup,
•	yields Row objects.

**RowBuilder**
•	takes the List<String> produced by the FSM,
•	ensures the values match the header layout,
•	returns an immutable Row.

**Row / Field / FieldType**
•	represent the data + types + validation,
•	used by higher layers (schema, validation, application logic).


________________________________________
