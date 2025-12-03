# CSV Data Processor – Group 5

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/actions)
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive.html)
[![Tests](https://img.shields.io/badge/tests-471-brightgreen.svg)](target/site/jacoco/index.html)
[![Coverage](https://img.shields.io/badge/coverage-92%25-brightgreen.svg)](target/site/jacoco/index.html)

**Atlantic Technological University – Software Development Project 2025**

**Course**: SWDE_IT803 - Software Development (2025/26)  
**Instructor**: Lusungu Mwasina  
**Degree**: Computing in Contemporary Software Development - Bachelor of Science (Honours)

A modular, production-grade Java library for reading, parsing, validating, transforming, and writing CSV files with clean object-oriented design and comprehensive test coverage.

## Table of Contents

- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Features](#features)
- [Usage](#usage)
- [Architecture](#architecture)
- [How It Works](#how-it-works)
- [Design Principles](#design-principles)
- [Development Approach](#development-approach)
- [Building and Testing](#building-and-testing)
- [Demo Application](#demo-application)
- [Development Workflow](#development-workflow)
- [Documentation](#documentation)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [Team Members](#team-members)
- [License](#license)

## Quick Start

### Clone and Build
```bash
git clone https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project.git
cd ATU-SoftDev-Grp5Project
mvn clean install
```

Expected output: `BUILD SUCCESS`

### Run Tests
```bash
mvn test
```

Expected output: All tests pass with `BUILD SUCCESS`

### Run Demo Application
```bash
mvn -q exec:java -Dexec.mainClass="com.group5.csv.demo.Main"
```

Expected output: Interactive CLI menu for CSV operations

## Project Structure

```
ATU-SoftDev-Grp5Project/
├── src/
│   ├── main/java/com/group5/csv/
│   │   ├── core/              # Row, Headers, FieldType
│   │   ├── io/                # CsvParser, CsvReader, CsvWriter
│   │   ├── schema/            # Schema, FieldSpec, Validators
│   │   ├── validation/        # Error handling
│   │   ├── exceptions/        # Custom exceptions
│   │   ├── ops/               # Filters, Transforms, Joins, Aggregations
│   │   └── demo/              # Demo CLI application
│   ├── main/resources/
│   │   ├── demo/              # Sample CSV files for demo
│   │   └── sample-csvs/       # Test data
│   └── test/java/com/group5/csv/
│       └── [Corresponding test packages]
├── docs/
│   ├── diagrams/              # PlantUML diagrams
│   ├── classes/               # Class documentation
│   ├── getting-started.md     # Setup guide
│   ├── pull-request-workflow.md
│   ├── JaCoCo-and-JUnit-setup-readme.md
│   └── how-csv-functions.md
├── pom.xml                    # Maven configuration
├── README.md                  # This file
└── LICENSE                    # Academic license
```

## Features

### CSV Reading
- Handles quotes, escapes, multiline fields, BOM, blank lines
- Configurable via `CsvConfig` and `CsvFormat`
- Support for multiple CSV dialects (RFC 4180, Excel, TSV, custom)

### CSV Writing
- Auto-quotes when required
- Quote-doubling and escaping
- Guarantees round-trip fidelity (Reader → Writer → Reader)

### Schema Validation
- Field types: String, Int, Decimal, Boolean, Date, DateTime, Time
- Custom validators: min/max, regex, required, etc.
- Structured error reporting with row/column/value context

### Streaming
- Memory-safe row streaming with `Spliterator<Row>`
- Ideal for multi-million-row files
- Lazy evaluation for performance

### Error Handling
- Rich exceptions with row, column, raw value, and message
- Fail-fast or accumulation modes
- Detailed validation error reporting

## Prerequisites

- **Java**: Java 21 (LTS version - required for this project)
- **Build Tool**: Maven 3.8.0 or higher
- **OS**: Linux, macOS, or Windows (WSL2 recommended for Windows)
- **RAM**: Minimum 2GB for builds, 4GB+ for large CSV processing

### Verify Prerequisites

```bash
# Verify Java version (should show "21.x.x")
java -version

# Verify Maven (should show "3.8.0" or higher)
mvn --version
```

## Installation

### Clone and Build

```bash
# Clone the repository
git clone https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project.git
cd ATU-SoftDev-Grp5Project

# Build the project
mvn clean install
```

## Usage

### As a Library

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.group5</groupId>
    <artifactId>csv-processor</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Reading CSV Files

```java
import com.group5.csv.io.CsvReader;
import com.group5.csv.core.Row;

try (CsvReader reader = CsvReader.fromPath(Paths.get("data.csv"))) {
    for (Row row : reader) {
        String name = row.get("name");
        int age = row.getInt("age");
        System.out.println(name + ": " + age);
    }
}
```

### Writing CSV Files

```java
import com.group5.csv.io.CsvWriter;
import com.group5.csv.core.Row;

try (CsvWriter writer = CsvWriter.toPath(Paths.get("output.csv"))) {
    writer.writeHeaders("name", "age");
    writer.writeRow("Alice", 30);
    writer.writeRow("Bob", 25);
}
```

### Schema Validation

```java
import com.group5.csv.schema.Schema;
import com.group5.csv.schema.FieldSpec;

Schema schema = Schema.builder()
    .field("name", FieldSpec.STRING)
    .field("age", FieldSpec.INT)
    .field("salary", FieldSpec.DECIMAL)
    .build();

try (CsvReader reader = CsvReader.fromPath(Paths.get("data.csv"), schema)) {
    for (Row row : reader) {
        // All fields are validated and typed
        System.out.println(row.get("name") + ": $" + row.getDecimal("salary"));
    }
}
```

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
│      Schema/Validation   │  ← Optional validation layer
└───────────┬─────────────┘
            │
┌───────────▼─────────────┐
│      CsvWriter/Printer   │  ← Escape/quoting + serializing
└─────────────────────────┘
```

### Core Components

- **CsvParser**: Finite State Machine (FSM) that tokenizes CSV input character-by-character
- **CsvReader**: Builds `Row` objects from parsed tokens
- **CsvWriter/CsvPrinter**: Handles escaping, quoting, and serialization
- **Schema**: Defines field types and validation rules
- **Row/Headers**: Data model for accessing typed fields

## How It Works

### Reading Pipeline

1. **Input Detection**: Detects BOM and charset automatically
2. **FSM Parsing**: Character-by-character parsing using Finite State Machine
   - Handles quoted fields, escaped quotes, embedded delimiters
   - Correctly processes multiline fields
3. **Row Construction**: Parsed cells are assembled into Row objects with Headers
4. **Optional Validation**: Schema validates and converts field types
5. **Streaming**: Rows consumed lazily via Iterator or Stream

### Writing Pipeline

1. **Row Input**: Rows passed to CsvWriter
2. **Formatting**: CsvPrinter applies quoting/escaping rules
3. **Serialization**: Consistent delimiter and quote rules applied
4. **Output**: Valid CSV written to file or stream

### Why Finite State Machine?

CSV parsing is stateful because of quoting rules:
- A comma inside quotes is NOT a field separator
- Escaped quotes ("") must be treated as a single character
- Newlines inside quotes must NOT end the record

The FSM tracks whether the parser is inside or outside a quoted field, ensuring correct parsing of complex CSV data.

## Design Principles

### Immutability & Thread Safety
- **Row** objects are immutable once created, making them safe to share across threads
- **RowBuilder** provides controlled construction while keeping Row simple and robust

### Separation of Concerns
- **I/O Layer** (`csv.io`): Handles streams, encoding, CSV dialects
- **Core Layer** (`csv.core`): Models rows, fields, headers, types
- **Schema Layer** (`csv.schema`): Encapsulates validation rules and constraints

### Type Safety
- **FieldType enum**: Centralizes parsing/formatting logic for each type
- **Field model**: Rich cell representation with validation state and error reporting
- **Specs** (DecimalSpec, DateSpec, etc.): Configure type-specific behavior

This design enables:
- Easy testing of isolated components
- Incremental feature development
- Clear error reporting with row/column/value context

## Development Approach

### Test-Driven Development (TDD)

The project was developed using Test-Driven Development methodology:

1. **Red-Green-Refactor Cycle**
   - Write failing test that defines desired functionality
   - Write minimal code to make test pass
   - Refactor while keeping tests passing

2. **Test-First Methodology**
   - Tests written before or alongside implementation
   - Forces clear thinking about interfaces and edge cases
   - Provides immediate feedback on design decisions

3. **Coverage Enforcement Strategy**
   - Gradual threshold increases: 30% → 40% → 50% → 60% → 70% → 80%
   - Prevents last-minute testing rushes
   - Maintains consistent testing discipline throughout development

### Team Collaboration

**Weekly Synchronous Meetings** (Tuesdays 18:30 via Microsoft Teams)
- Progress review and design clarification
- Problem-solving and blocker resolution
- All meetings recorded for asynchronous access

**Daily Asynchronous Communication** (WhatsApp)
- Quick questions and updates
- Team decision-making via polls
- Coordination on interdependent tasks

**GitHub-Based Workflow**
- Feature branches with defined naming conventions
- Pull requests reviewed by peers before merge
- Commit messages reference GitHub Issues for traceability
- Mandatory code review ensures quality and knowledge sharing

### Testing Challenges Solved

**Challenge 1: Finite State Machine (FSM) Parser**
- Solution: RFC 4180 compliance testing with comprehensive edge cases
- Result: 98% instruction coverage, 91% branch coverage

**Challenge 2: Large File Streaming**
- Solution: Iterator and streaming API tests for memory efficiency
- Result: Efficient processing without memory overflow

**Challenge 3: Round-Trip Consistency**
- Solution: Integration tests comparing original and output files
- Result: Data integrity preserved across read-write cycles

### Quality Metrics

- **471 test methods** across 25 test files
- **92% code coverage** (exceeded 80% requirement)
- **100% test success rate** (452 tests passed, 0 failures)
- **5.4 second test execution** (rapid feedback cycle)
- **47 merged pull requests** with peer review
- **100% team meeting attendance** (5 meetings, all recorded)

## Building and Testing

### Build the Project

```bash
mvn clean install
```

### Run Unit Tests

```bash
mvn test
```

### Run All Tests (including integration tests)

```bash
mvn verify
```

### Generate Code Coverage Report

```bash
mvn clean test jacoco:report
```

View the report at: `target/site/jacoco/index.html`

### Test Statistics

- **471 test methods** across 25 test files
- **92% code coverage** enforced via JaCoCo
- **Comprehensive coverage** of all core packages:
  - `com.group5.csv.core` (97% coverage)
  - `com.group5.csv.io` (98% coverage)
  - `com.group5.csv.schema` (79% coverage)
  - `com.group5.csv.exceptions` (93% coverage)
  - `com.group5.csv.demo` (100% coverage)

## Demo Application

The project includes an interactive CLI demo that showcases library features.

### Run the Demo

```bash
mvn -q exec:java -Dexec.mainClass="com.group5.csv.demo.Main"
```

### Demo Features

- Load a CSV file
- Display rows in formatted table
- Validate using schema
- Perform round-trip (read → write → read)
- Test different CSV dialects (comma, semicolon, tab)

### Sample Data

Demo CSV files are located in `src/main/resources/demo/`:
- `demo_input.csv` (comma-delimited)
- `demo_input_semicolon.csv` (semicolon-delimited)
- `demo_input_tab.tsv` (tab-delimited)

## Development Workflow

### Creating a Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### Committing Changes

```bash
git add .
git commit -m "feat: add your feature description"
```

### Running Tests Before Commit

```bash
mvn verify
```

### Pushing and Creating a Pull Request

```bash
git push origin feature/your-feature-name
```

Then open a Pull Request on GitHub. See [Pull Request Workflow](docs/pull-request-workflow.md) for details.

### Code Quality Standards

- All tests must pass: `mvn verify`
- Code coverage must remain ≥80%
- Follow Java naming conventions
- Write meaningful commit messages

## Documentation

| Document | Description |
|----------|-------------|
| [Getting Started Guide](docs/getting-started.md) | Step-by-step setup instructions |
| [Pull Request Workflow](docs/pull-request-workflow.md) | How to open and merge PRs |
| [JaCoCo & JUnit Setup](docs/JaCoCo-and-JUnit-setup-readme.md) | Testing framework configuration |
| [How CSV Functions](docs/how-csv-functions.md) | Detailed CSV processing pipeline |
| [Design Rationale](docs/DesignRationale-Core.md) | Architecture and design decisions |
| [Class Documentation](docs/classes/) | Individual class documentation |
| [Diagrams](docs/diagrams/) | PlantUML architecture diagrams |

## Troubleshooting

### Issue: Java version mismatch

**Error**: `Unsupported class version 65.0` or compilation errors

**Solution**: Ensure you have Java 21 installed.

```bash
java -version
```

Expected output: `java version "21.x.x"`

### Issue: Maven build fails with dependency error

**Error**: `Could not find artifact...`

**Solution**: Update your local repository and clear Maven cache.

```bash
mvn clean dependency:resolve
```

### Issue: Tests fail with "Cannot find test resources"

**Error**: `FileNotFoundException: demo_input.csv`

**Solution**: Ensure you're running tests from the project root directory.

```bash
cd ATU-SoftDev-Grp5Project
mvn test
```

### Issue: JaCoCo report not generated

**Solution**: Run the full verify lifecycle:

```bash
mvn clean verify
```

Report will be at: `target/site/jacoco/index.html`

## Contributing

We welcome contributions! Please see [Pull Request Workflow](docs/pull-request-workflow.md) for detailed guidelines.

Quick summary:
1. Create a feature branch: `git checkout -b feature/YourFeature`
2. Make your changes and write tests
3. Run `mvn verify` to ensure all tests pass
4. Commit with meaningful messages
5. Push and open a Pull Request
6. Address review feedback
7. Merge using "Squash and Merge"

## Team Members

- Michael McKibbin
- Bogdan Bondarenko
- Vivien White
- Edson Soares Ferreira
- Arron Hoare
- Abudulqurdiri Adelakun

## License

This project is for academic assessment as part of:
- **Course**: SWDE_IT803 - Software Development (2025/26)
- **Institution**: Atlantic Technological University (ATU)
- **Degree**: Computing in Contemporary Software Development - Bachelor of Science (Honours)
- **Instructor**: Lusungu Mwasina

You may reuse parts of it as reference material in future coursework or portfolios, provided you maintain attribution to the original authors and Group 5.

---

**Last Updated**: December 3, 2025  
**Project Status**: Active Development  
**Maintained By**: Group 5 - ATU Software Development
