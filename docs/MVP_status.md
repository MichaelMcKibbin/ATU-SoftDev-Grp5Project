# Minimum Viable Product (MVP)

We are very close — about 80–90% complete.

As for a complete product, we have a long way to go. 
We're at about 50% of the way.
A complete product is impossible in the time remaining, but we can achieve a (more) complete MVP.

## MVP means:
- The product solves the core problem reliably.
- Users can perform the primary tasks.
- Bugs are limited; behaviour is predictable.
- Key features are implemented cleanly.

## What we already have that contributes to MVP
### Core capabilities
- Read CSV consistently.
- Write CSV via writer.
- Round-trip CSV fully.
- Handle multiple dialects (RFC4180, Excel, Excel-semicolon, TSV, JSON-CSV).
- Header override support.
- Configurable processing options. 

### Data model
- Headers class
- Row model
- CsvConfig builder
- CsvFormat & dialect management
- VirtualReader test utility
We’ve built a completely modular CSV architecture.

### Testing
- Good coverage on config, reader, writer primitives.
- CLI input-validation tests.
- Enough coverage to safely pass CI & JaCoCo.

### Documentation
- Good Javadoc coverage throughout core classes. (More would be nice.)
- Descriptive PR messages.
- Strong internal comments.

### Code organization
Clean separation between:
- I/O primitives
- Parsing logic
- Printing/writing logic
- Demo/CLI layer

## What is optional but nice to complete MVP
Some fairly small items
- Add a “read the entire file” option without the preview limit
(maxLines = -1 exists, so basically done, easy enough to extend functionality)
- Possibly add more user-friendly error messages.
- Add a test for CsvWriter.writeAllRows() using VirtualReader + temp file
- Maybe a “validate CSV schema” stub (currently option 3 is placeholder)

### Verdict:

We're very close, maybe a day’s work from a fully complete MVP.
For assignment purposes, we may already qualify as MVP.