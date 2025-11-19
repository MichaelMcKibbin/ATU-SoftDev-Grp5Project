## Core Module (`com.group5.csv.core`)

The **core module** defines the in-memory representation of CSV data and the
type system used by the rest of the project. It is intentionally independent of
I/O concerns so it can be reused by multiple front-ends (CLI, GUI, libraries).

### Key Classes

- **`Headers`**
    - Represents the logical column layout (header row).
    - Provides name-to-index lookup and column existence checks.
    - Defines the canonical ordering of columns used by `Row` and `RowBuilder`.

- **`Row`**
    - Immutable representation of a single CSV record.
    - Holds a `Headers` reference and an ordered set of values/fields.
    - Accessors typically include:
        - `get(int index)`
        - `get(String columnName)`
        - `size()`, `headers()`, etc.

- **`Field`**
    - Represents a single cell within a `Row`.
    - Exposes:
        - Raw text: `raw()`
        - Typed value: `value()`, `valueAs(Class<T>)`
        - Metadata: `index()`, `name()`
        - Validation state: `isMissing()`, `isValid()`, `errors()`
        - Type-specific specs: `decimalSpec()`, `dateSpec()`, `dateTimeSpec()`
    - Designed to be immutable and safe for lazy parsing + memoization.

- **`FieldType`**
    - Enum describing the supported logical types:
        - `STRING`, `INT`, `LONG`, `DOUBLE`, `DECIMAL`, `BOOLEAN`, `DATE`, `DATETIME`
    - Responsible for parsing & formatting:
        - `Object parse(String raw, Field field)`
        - `String format(Object value, Field field)`
    - Uses the specs exposed by `Field` for complex types such as decimal and date/time.

- **`RowBuilder`**
    - Mutable helper class to construct `Row` instances safely.
    - Tied to a `Headers` instance, which defines the expected number and order of columns.
    - Collects raw values via:
        - `add(String value)`
        - `addAll(List<String> values)`
        - `set(int index, String value)`
        - `set(String name, String value)`
    - Enforces that the number of collected values matches `headers.size()` before
      building a `Row`.

### Row Construction Workflow

1. **Headers created**  
   Usually from the first line of the CSV file:
```java
Headers headers = new Headers(List.of("id", "name", "age"));
```

2. **RowBuilder created for these headers**
```java
RowBuilder builder = new RowBuilder(headers);
```

3. **Values added as the CSV reader parses a line**
```java
builder.add("1")
       .add("Jim")
       .add("40");
```

4. **Immutable Row built**
```java
Row row = builder.build();
```

5. **Row consumed via index or column name**
```java
String name = row.get("name");   // "Alice"
int age = Integer.parseInt(row.get("age"));
```

This separation of concerns keeps parsing/validation logic cleanly separated from
I/O, and makes it easy to unit test each part of the system (Headers, Field,
FieldType, Row, RowBuilder) in isolation.
