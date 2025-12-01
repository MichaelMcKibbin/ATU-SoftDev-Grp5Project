# MVP Completion Plan Ideas



Read a CSV (with header / without header)

Change dialect and charset

Override header list

Set maxLines to unlimited and to small numbers

Confirm behaviour matches expectations

## To do

Remove old commented-out placeholders\code

## Test It

Project compiles with no warnings (or deprecated references if possible).


## Finalise CSV Reading & Writing Behaviour

Confirm that CsvReader:
- correctly interprets header/no-header
- handles delimiters, quotes, escapes
- handles empty-line behavior per config

Confirm that CsvWriter:
- writes fields quoted/escaped correctly
- uses the config’s charset
- respects the delimiter from the dialect

### Headers - on or off by default?

Default writes only data rows, the header is not rewritten.
(hasHeader means “reader consumes header”)

Or 

Should we write header row too (one extra line of code?)

There is the option to change the setting in the menu, so maybe this is already covered?


### Test round-trip manually on:

- RFC4180 CSV
- Excel semicolon CSV
- TSV

And verify the output looks correct.


### Validation Logic

#### Option 3 in menu should do something, minimal is OK.

Implement a very small schema-validation command.

Perhaps something along these lines?
```
private static void demoValidateCsv() {
if (lastRows == null) {
System.out.println("No CSV loaded. Please read a CSV first.");
return;
}

    boolean successes = true;
    for (Row row : lastRows) {
        if (row.size() != lastHeaders.size()) {
            System.out.println("Row length mismatch: " + row);
            successes = false;
        }
    }

    if (successes)
        System.out.println("CSV appears structurally valid.");
}
```

This gives more functionality for minimal effort

To test it:

Load a file with inconsistent row lengths → see a validation error
Load a clean file → success messaage “CSV appears structurally valid.”

### Finalise Unit Tests 
Final coverage targets:

- 80%+ on non-CLI core packages (demo/Main excluded)

- 50% or more, if possible, on demo/Main (CLI entry point)


## Documentation

Include in README:

- Project description
- How to build & run
- Features
- Supported CSV dialects
- Screenshots of CLI menus (or text is fine)
- Examples of reading and writing CSV
- Testing instructions
- Known limitations

Optional:

Add architecture docs with:
- class diagram
- responsibilities per class
- example flow of CsvReader → Row → CsvWriter


When all that is done, mark the MVP as complete and stable.
- Ensure GitHub Actions build is green
- Create a final PR with a message like: “MVP completion: CSV read/write pipeline, dialect support, CLI menu, smoke tests, validation”
- After merge, tag a release:
```
git tag v1.0-MVP
git push origin v1.0-MVP
```


### Summary: What remains to reach MVP?

- Implement minimal validation for option 3
- Clean up the main menu + finalize demoReadMenu consistency
- Write 1–2 small CsvReader/CsvWriter tests (not CLI)
- Write final README
- Remove legacy placeholder code
- Ensure build is clean and all PRs merged
