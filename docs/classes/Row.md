# Row Class - Design Documentation

**Author**: Edson Ferreira  
**Date**: November 15, 2025  
**Issue**: [#10 - Row](https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/issues/10)  
**Package**: `com.group5.csv.core`

---

## What It Does

Row represents one row of CSV data. It works with Headers to let you access values by column name.

```java
Headers headers = new Headers("id", "name", "age");
Row row = new Rows(headers, Arrays.asList("1", "Alice", "25"));

String name = row.get("name");  // "Alice"
String age = row.get(2);        // "25" (by index)
```

---

## Key Features

- **Works with Headers**: Access data by column name or index
- **Immutable**: Can't change after creation (thread-safe)
- **Allows null**: Represents missing data (e.g., empty CSV fields)
- **Fast**: O(1) lookup for all operations

---

## How It Works with Headers

**Headers** knows column positions:
```
"name" → index 1
```

**Rows** holds the data:
```
["1", "Alice", "25"]
```

**Together**:
```java
row.get("name")  →  Headers finds index 1  →  Returns "Alice"
```

---

## Design Choices

### Why Immutable?
- Thread-safe
- No accidental changes
- Matches Headers pattern

### Why Allow Null?
CSV files have missing data:
```csv
id,name,age
1,Alice,     ← age is missing
```

```java
Row row = new Rows(headers, Arrays.asList("1", "Alice", null));
assertNull(row.get("age"));  // null = missing
```

### Why Size Validation?
Prevents data corruption:
```java
Headers headers = new Headers("id", "name", "age");  // 3 columns

new Rows(headers, Arrays.asList("1", "Alice"));  // ❌ Only 2 values - throws error
```

---

## API Reference

### Constructor
```java
public Rows(Headers headers, List<String> values)
```
Creates a row. Values must match header count.

**Throws**: `IllegalArgumentException` if null or size mismatch

---

### Get Value by Name
```java
public String get(String columnName)
```
Returns value for column name (may be null).

**Example**:
```java
String name = row.get("name");  // "Alice"
```

---

### Get Value by Index
```java
public String get(int index)
```
Returns value at position (may be null).

**Example**:
```java
String name = row.get(1);  // "Alice"
```

---

### Other Methods
```java
public int size()                    // Number of values
public boolean isEmpty()             // True if no values
public List<String> getValues()      // All values (unmodifiable)
public Headers getHeaders()          // Associated headers
```

---

## Usage Examples

### Basic Access
```java
Headers headers = new Headers("id", "name", "age");
Row row = new Rows(headers, Arrays.asList("1", "Alice", "25"));

row.get("name");   // "Alice"
row.get(0);        // "1"
row.size();        // 3
```

### Handling Missing Data
```java
Row row = new Rows(headers, Arrays.asList("1", "Alice", null));

String age = row.get("age");
if (age == null) {
    System.out.println("Age missing");
}
```

### Iterating Values
```java
for (int i = 0; i < row.size(); i++) {
    String name = headers.getName(i);
    String value = row.get(i);
    System.out.println(name + ": " + value);
}
```

### Real-World CSV (with Headers enhancements)
```java
// Messy header: "  ID  ,Name, AGE "
Headers headers = new Headers(Arrays.asList("  ID  ", "Name", " AGE "));
Row row = new Rows(headers, Arrays.asList("1", "Alice", null));

// All work (trimmed + case-insensitive):
row.get("id");     // "1"
row.get("ID");     // "1"
row.get("name");   // "Alice"
row.get("NAME");   // "Alice"
row.get("age");    // null (missing)
```

---

## Testing

**20 tests covering**:
- Constructor validation (null checks, size mismatch)
- Get by name and index
- Null value handling
- Edge cases (single value, 100+ values)
- Integration with Headers (whitespace, case-insensitive)

**Coverage**: 95%+

---

## Performance

All operations are O(1) (constant time):
- `get(String)` - Uses Headers' Map lookup
- `get(int)` - Direct List access
- `size()`, `isEmpty()`, `getValues()`, `getHeaders()` - Simple field access

---

## Comparison: Headers vs Rows

| | Headers | Row |
|---|---|---|
| **Holds** | Column names | Data values |
| **Quantity** | One per CSV | Many per CSV |
| **Purpose** | Schema | Data |
| **Null values** | No | Yes (missing data) |

---

## Related Classes

- **Headers** (#11) - Column name mapping
- **CsvReader** (#1) - Creates Row from CSV
- **RowBuilder** (#19) - Validates and builds Rows
- **Schema** (#12) - Validates Row data

---

## Questions?

- Comment on [Issue #10](https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/issues/10)
- Ask in team chat
- Discuss in team meeting

---

**Status**: Complete  
**Updated**: 2025-11-15
