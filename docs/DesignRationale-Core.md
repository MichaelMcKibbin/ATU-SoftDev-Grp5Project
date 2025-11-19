# Design Rationale – Core Model (Row, Field, FieldType, RowBuilder)

This section explains the main design decisions in the com.group5.csv.core package: why rows are immutable, why a separate builder is used, and why the type system is modelled with an enum-based FieldType.

## 1. Immutable Row: Safety, Simplicity, and Reasoning

**Row** is designed as an immutable value object:

A row is constructed once (via **RowBuilder**) and then exposed read-only.

Callers can safely share Row instances without worrying about accidental modification.

Immutability makes it easier to:

reason about program behaviour,

avoid subtle bugs caused by shared mutable state,

use rows safely in caches, collections, or parallel processing.

From an API perspective, a Row represents “the data as read from the CSV” at a specific point in time. Once parsed and validated, it should not change under the caller’s feet.


## 2. RowBuilder: A Controlled Construction Process

Instead of allowing **Row** to be constructed directly in a piecemeal way, a dedicated **RowBuilder** is used as a mutable helper.

_Motivations:_

Parsing a CSV record is naturally incremental:

the parser reads one cell at a time,

we do not have all values upfront.

RowBuilder provides a controlled place to accumulate these values while:

enforcing that the final number of values matches the header schema,

keeping temporary mutable state out of Row itself,

preventing partially constructed or malformed rows from leaking into the rest of the system.

This separation yields a clean workflow:

**Headers** defines the expected columns.

**RowBuilder** accumulates raw string values as the parser reads a row.

**RowBuilder.build()** performs a final consistency check and returns a fully-formed, immutable Row.

This design keeps Row simple and robust, while allowing the parser layer (CsvReader) to remain efficient and flexible.


## 3. Field and FieldType: Clear Type Semantics

Each cell within a row is modelled as a Field, which combines:

the raw text from the CSV,

metadata (index, name),

validation state (missing, errors),

and a logical type represented by FieldType.


### Why an enum FieldType?

Using an enum for FieldType (e.g. STRING, INT, DECIMAL, DATE, DATETIME) has several advantages:

Centralised parsing/formatting logic
Each enum constant implements parse(raw, field) and format(value, field):

logic for integers, dates, decimals, etc. is kept in one place,

behaviour is consistent across the entire application.

Discoverability and constraints
The set of supported types is explicit and finite:

users of the API can see at a glance what is supported,

there is no ambiguity over “what types exist”.

Integration with specs
Complex types like decimal and datetime depend on configuration objects:

DecimalSpec for precision/scale and formatting,

DateSpec / DateTimeSpec / TimeSpec for patterns and allowed formats.
FieldType uses the specs exposed by Field (decimalSpec(), dateSpec(), etc.) to perform type-aware parsing and formatting.

This design gives strong separation between:

What a type is (FieldType enum), and

How that type is configured (DecimalSpec, DateSpec, DateTimeSpec).

## 4. Field as a Rich Cell Model

Field acts as the “single cell façade” over raw data, type, and validation:

Provides access to:

- raw() – original text,
- value() / valueAs(Class<T>) – the parsed value,
- isMissing(), isValid(), and errors() – validation state.

Delegates type-specific behaviour to FieldType and specs.

Design benefits:

Callers can work at the right abstraction level:

treat a field as raw text (for generic transformations), or

treat it as a typed value (for business logic).

Validation information is localised at cell level, which makes error reporting and debugging much easier (e.g. “row 10, column ‘age’ has invalid integer”).

This sets up a good foundation for schema-based validation and richer error reporting in later iterations.


## 5. Separation of Concerns and Testability

Overall, the design separates concerns cleanly:

I/O layer (csv.io)

Deals with streams, encoding, and CSV dialect details.

Core layer (csv.core)

Models rows, fields, headers, and types — independent of I/O.

Schema/validation layer (csv.schema)

Encapsulates rules such as formats and constraints.


Tests are split into three parts:

Because these responsibilities are isolated, each part is straightforward to unit test:

FieldTypeTest focuses on parsing/formatting behaviour.

RowBuilderTest ensures row construction logic is correct and rejects malformed rows.

FieldTest verifies default behaviour such as typed value access and error handling.

This structure supports incremental development: as features like schema validation or more complex field types are added, they can be slotted into the existing model without major redesign.