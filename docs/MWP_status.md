# Minimum Working Prototype (MWP)

**We have already passed this stage.**

## MWP means:

### The core idea is demonstrated end-to-end.
- It works, in a basic way.
- It proves feasibility.
- It does not need full error-handling or configurability.

## This project already includes:

### CSV reading (via CsvReader)
- Supports configurable charset, dialect, header presence, empty-line policy.
- Uses real CsvReader implementation, not placeholders.
- Can read any CSV into List<Row>.

### CSV writing (via CsvWriter)
- Round-trip pipeline is functional.
- Can produce a new CSV file consistent with RFC-4180 rules.

### Interactive configuration menu
- Lets the user change charsets, headers, dialects, skip-empty-lines, max preview lines.
- Demonstrates real control over reader behaviour.

### Demonstrations of features
- Round-trip operations
- Read preview
- Sample write
- Dialect switching
All valid MWP features.

### Working tests
- Mid-level CLI flow tests.
- Unit tests for helper functions.

### Verdict:
- MWP achieved and exceeded.
- The project is already more advanced than a typical MWP.