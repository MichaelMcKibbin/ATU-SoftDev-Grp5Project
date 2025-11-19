### CSV FSM Transition Table (per record)

Assumptions:

- DELIM = delimiter (e.g. , or ;)
- QUOTE = quote char (e.g. "), as per CsvFormat
- NL = newline (LF or CRLF normalised)
- CHAR = any other character (including spaces, digits, letters, etc.)
- EOF = end-of-file

Note: “emit(field)” means append the current field buffer to the list of cells for this record.

| Current State      | Input    | Next State          | Action(s)                                                                                  |
|--------------------|----------|---------------------|--------------------------------------------------------------------------------------------|
| **OUTSIDE_FIELD**  | DELIM    | OUTSIDE_FIELD       | emit(empty field); start new empty field                                                   |
| OUTSIDE_FIELD      | QUOTE    | IN_QUOTED_FIELD     | start new field; clear buffer                                                              |
| OUTSIDE_FIELD      | NL       | [END]               | emit(empty field); end record                                                              |
| OUTSIDE_FIELD      | EOF      | [END/EOF]           | if no data: return null (no more records); else emit(empty field) and end record          |
| OUTSIDE_FIELD      | CHAR     | IN_UNQUOTED_FIELD   | start new field; buffer ← CHAR                                                             |
| **IN_UNQUOTED_FIELD** | DELIM | OUTSIDE_FIELD       | emit(buffer); clear buffer; expect next field                                              |
| IN_UNQUOTED_FIELD  | NL       | [END]               | emit(buffer); end record                                                                   |
| IN_UNQUOTED_FIELD  | EOF      | [END/EOF]           | emit(buffer); end record (last line may not end with newline)                             |
| IN_UNQUOTED_FIELD  | CHAR     | IN_UNQUOTED_FIELD   | buffer ← buffer + CHAR                                                                     |
| IN_UNQUOTED_FIELD  | QUOTE    | IN_UNQUOTED_FIELD   | buffer ← buffer + QUOTE (quote has no special meaning in unquoted field)                   |
| **IN_QUOTED_FIELD**| QUOTE    | AFTER_QUOTE         | potential end of quoted field; do not emit yet                                            |
| IN_QUOTED_FIELD    | CHAR     | IN_QUOTED_FIELD     | buffer ← buffer + CHAR                                                                     |
| IN_QUOTED_FIELD    | DELIM    | IN_QUOTED_FIELD     | buffer ← buffer + DELIM (delimiter is literal inside quotes)                               |
| IN_QUOTED_FIELD    | NL       | IN_QUOTED_FIELD     | buffer ← buffer + NL (embedded newline is part of field)                                   |
| IN_QUOTED_FIELD    | EOF      | [ERROR or END]      | malformed CSV (unterminated quote); depending on mode: error or treat as end of field     |
| **AFTER_QUOTE**    | QUOTE    | IN_QUOTED_FIELD     | escaped quote: buffer ← buffer + QUOTE                                                     |
| AFTER_QUOTE        | DELIM    | OUTSIDE_FIELD       | emit(buffer); clear buffer; expect next field                                              |
| AFTER_QUOTE        | NL       | [END]               | emit(buffer); end record                                                                   |
| AFTER_QUOTE        | EOF      | [END/EOF]           | emit(buffer); end record (field ends at EOF)                                               |
| AFTER_QUOTE        | CHAR     | [ERROR]             | invalid CSV: non-delimiter, non-newline character after closing quote                     |

[END] → return the completed list of fields for this record

[END/EOF] → same as [END], but caller also knows file is finished

[ERROR] → behaviour depends on strict/lenient mode