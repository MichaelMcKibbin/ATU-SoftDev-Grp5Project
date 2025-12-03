# CSV Data Processor – Group 5

### Atlantic Technological University – Software Development Project 2025


## Table of Contents

- Project Overview
- Features
- Architecture
- Class Diagrams
- How It Works
- Quick Start
- Using the Library (Code Examples)
- Demo Application
- Testing & Coverage
- Development Workflow
- Documentation
- Team Members
- License

## Project Overview

The CSV Data Processor is a modular Java library for reading, parsing, validating, transforming, and writing CSV files.
It is designed with clean, maintainable object-oriented principles and supports:

- Multiple CSV dialects (RFC 4180, Excel, TSV, custom)
- Schema-based typing & validation (string, int, decimal, date, etc.)
- Structured error reporting
- Streaming support for large files
- Round-trip consistency (Reader → Writer → Reader)

The project demonstrates team collaboration, software engineering best practices, and professional testing strategies.

## Features
### CSV Reading

Handles quotes, escapes, multiline fields, BOM, blank lines

Configurable via CsvConfig and CsvFormat

### CSV Writing

Auto-quotes when required

Quote-doubling and escaping

Guarantees round-trip fidelity

### Schema Validation

Field types (String, Int, Decimal, Boolean, Date, DateTime)

Custom validators (min/max, regex, required, etc.)

### Streaming

Memory-safe row streaming with Spliterator<Row>

Useful for multi-million-row files

### Error Handling

Rich exceptions: 
- row, 
- column, 
- raw value, 
- message

Supports fail-fast or accumulation modes

### Demo Application

A CLI tool showing library behaviours:

- Load CSV
- View rows
- Validate with schema
- Perform round-trip
- Test dialect variations

## Architecture

The system follows a layered design:
```

┌─────────────────────────┐
│   CsvConfig / CsvFormat │  ← Dialect + Policy
└───────────┬─────────────┘
│
┌───────────▼─────────────┐
│       CsvParser (FSM)    │  ← Tokenizes CSV input
└───────────┬─────────────┘
│
┌───────────▼─────────────┐
│        CsvReader         │  ← Builds Row objects
└───────────┬─────────────┘
│
┌───────────▼─────────────┐
│ Row, Headers, FieldType │  ← Data model & typed access
└───────────┬─────────────┘
│
┌───────────▼─────────────┐
│      Schema/Validation   │
└───────────┬─────────────┘
│
┌───────────▼─────────────┐
│      CsvWriter/Printer   │  ← Escape/quoting + serializing
└─────────────────────────┘
```

## Class Diagrams TODO

**TODO: Add screenshots from IntelliJ UML, Mermaid diagrams, etc.**

Core Architecture Diagram
(Insert diagram here)

Parser & Reader Interaction Diagram
(Insert diagram here)

Data Model (Row, Headers, FieldType)
(Insert diagram here)

Schema & Validation
(Insert diagram here)

Any others???



## How It Works
1. Reader Pipeline
- Reader detects BOM
- Parser FSM walks characters one at a time
- Tokens become rows (Row)
- Headers are extracted if enabled
- Schema (optional) validates & converts fields
- User iterates rows or streams them

2. Writer Pipeline

- Rows passed to CsvWriter
- CsvPrinter quotes/escapes values
- Consistent delimiter/quote rules applied
- Valid CSV written to disk/output stream


3. Streaming

Rows are consumed lazily using:
- Stream<Row> stream = StreamSupport.stream(reader.spliterator(), false);




## Demo Application

Run the interactive CLI:
```
mvn -q exec:java -Dexec.mainClass="com.group5.csv.demo.Main"
```

### Features include:
- Load a CSV
- Display rows
- Validate using schema
- Write round-trip output
- Test dialects (comma/semicolon/tab)

## Testing & Coverage
- 400+ unit and integration tests
- 80–90% coverage enforced via JaCoCo 

### Coverage report: 
```
target/site/jacoco/index.html
```

### CI pipeline validates:
- build
- unit tests
- coverage threshold
- pull request integrity

## Development Workflow

- Feature branches
- Meaningful commit messages
- Pull requests + review required
- CI must pass before merge
- Squash & Merge into main
- Main always deployable & stable

This mirrors industry best practices (GitHub Flow).

## Documentation

| Document                      | Description                                   |
| ----------------------------- | --------------------------------------------- |
| **Testing Strategy Report**   | How we planned, executed, and validated tests |
| **Technical Design Document** | Architecture, algorithms, design decisions    |
| **Collaboration Report**      | How the team worked together                  |
| **Proposal Document**         | Original design and scope                     |
| `/docs/*`                     | Setup guides, workflow docs, diagrams         |


## Team Members

- Michael McKibbin
- Bogdan Bondarenko
- Edson Ferreira
- Vivien White
- Abudulqurdiri Adelakun
- Aaron Hoare


## License

This project is for academic assessment as part of ATU’s Software Development module.
You may reuse parts of it as reference material in future coursework or portfolios.