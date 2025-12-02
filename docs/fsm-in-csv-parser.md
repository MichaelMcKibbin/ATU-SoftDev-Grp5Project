# A short explanation of the Finite State Machine in the CSV Parser

A finite state machine (FSM) is used in CSV parsing to correctly handle the different situations a parser encounters while reading characters one by one.
CSV looks simple, but because of quoting rules, embedded commas, escaped quotes, and newlines inside quoted fields, a parser can’t just “split by comma”. It needs to track its state.

In this project, the FSM ensures that CsvReader correctly identifies:
- where a field starts
- how characters inside quotes behave
- when a delimiter actually separates fields
- when a newline truly ends a record
- how to handle escaped quotes ("")
- when malformed input should be detected
________________________________________

## Typical States in the CSV FSM
A standard CSV FSM uses states like:
1. OUTSIDE_FIELD
You’re between fields, expecting the start of a new field.
The next character decides how the field begins.

2. IN_UNQUOTED_FIELD
You’re reading characters normally until:
•	a comma ends the field
•	newline ends the record

3. IN_QUOTED_FIELD
Field began with a ".
Everything is literal except:
•	"" → an escaped quote
•	a single " → may indicate end of quoted field

4. AFTER_QUOTE
You just closed a quoted field but haven’t processed what comes next.
The only valid characters here are:
•	delimiter → field ends
•	newline → record ends
Anything else → malformed CSV.
________________________________________
### Why an FSM Is Needed in This Project
- CSV rules (especially RFC 4180) are stateful: meaning “a quote here” means something different depending on what state you were in.
- A field like this: "hello, world" contains a comma that is not a separator — the FSM keeps track of being inside a quote.
- Newlines inside quotes must not end the record.
- Escaped quotes ("") must be treated as a single literal character.

Without an FSM, the parser would misinterpret many legal CSV inputs, producing truncated fields, extra columns, or broken records.
________________________________________

### How This Fits into the Project
- The FSM concept belongs inside CsvParser and ultimately CsvReader, (even if not fully implemented yet).
- RowBuilder then receives the parsed list of raw cell values after the FSM has assembled them properly.
- FieldType and validation logic operate after FSM parsing.

So the flow looks like:
raw chars → FSM → List<String> cells → RowBuilder → Row → FieldType → Validation
________________________________________

### Summary
To summarise, the CSV parser uses a finite state machine to correctly interpret fields, quoted values, delimiters, and newlines. The FSM ensures that commas inside quotes, escaped quotes, and multi-line fields are handled properly by tracking whether the parser is inside or outside a quoted field. This prevents incorrect splitting and guarantees standards-compliant parsing before values are passed to RowBuilder.


