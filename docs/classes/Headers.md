# Headers Class - Design Documentation

**Author**: Edson Ferreira  
**Date**: November 11, 2025 (Updated: November 15, 2025)  
**Issue**: [#11 - Header](https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/issues/11)  
**Package**: `com.group5.csv.core`

---

## Purpose

The `Headers` class manages the mapping between CSV column names and their positional indexes. It provides fast, bidirectional lookup between column names (e.g., "age") and their positions (e.g., column 2).

**Key Features**:
- **Whitespace trimming**: Automatically removes leading/trailing spaces from column names
- **Case-insensitive lookup**: Find columns regardless of capitalization (e.g., "Name", "name", "NAME" all work)
- **Original case preservation**: Keeps the original column name format for display and writing

**Used by**: `Row`, `CsvReader`, `CsvWriter`, `Schema` for consistent column access, validation, and ordering.

---

## The Problem It Solves

### Real-World Example

Given this CSV:
```csv
id,name,age,city
1,Alice,25,Dublin
2,Bob,30,Cork
```

**Challenges**:
1. **Human thinking**: "What's Alice's age?" (think in names)
2. **Computer storage**: Data is in arrays: `["1", "Alice", "25", "Dublin"]` (positions)
3. **Need translation**: "age" → position 2 → value "25"

**Without Headers**: Every class would need to search through column names repeatedly (slow, error-prone)

**With Headers**: Centralized, fast lookup service for the entire application

---

## How Headers Knows the Number of Columns

### The Short Answer
**Headers doesn't figure it out - CsvReader tells it!**

### The Complete Flow

#### Step 1: CsvReader Reads the First Line

```java
// CsvReader opens the file
BufferedReader reader = new BufferedReader(new FileReader("students.csv"));

// Read the very first line
String firstLine = reader.readLine();
// firstLine = "id,name,age,city"
```

#### Step 2: CsvReader Splits the Line into Columns

```java
// Split by comma
String[] columns = firstLine.split(",");
// columns = ["id", "name", "age", "city"]

// How many columns? Check the array length!
System.out.println(columns.length);  // 4
```

#### Step 3: CsvReader Creates Headers

```java
// Convert array to List
List<String> columnList = Arrays.asList(columns);

// Give it to Headers
Headers headers = new Headers(columnList);
```

#### Step 4: Headers Counts What It Receives

```java
public Headers(List<String> columnNames) {
    // Headers just counts what it was given
    this.columnNames = new ArrayList<>(columnNames);
    
    // The size is determined by the input
    // 3 columns? size() returns 3
    // 30 columns? size() returns 30
}
```

### Visual Example

**CSV with 3 Columns**:
```csv
id,name,age
1,Alice,25
```

**What happens**:
```
File → CsvReader reads "id,name,age"
     → Split by comma: ["id", "name", "age"]
     → Array length: 3
     → Headers receives List with 3 items
     → headers.size() returns 3
```

**CSV with 30 Columns**:
```csv
id,name,age,city,email,phone,address,zip,country,state,...(20 more)
1,Alice,25,Dublin,...
```

**What happens**:
```
File → CsvReader reads entire first line
     → Split by comma: ["id", "name", "age", ..., "column30"]
     → Array length: 30
     → Headers receives List with 30 items
     → headers.size() returns 30
```

### Key Insight: Separation of Concerns

**Headers is "dumb" in a good way!**

It doesn't:
- ❌ Read files
- ❌ Parse CSV
- ❌ Count commas
- ❌ Guess the structure

It only:
- ✅ Receives a list of column names
- ✅ Stores them
- ✅ Provides lookup methods

**This is called "Separation of Concerns"**:
- **CsvReader's job**: Read files, parse CSV, handle commas/quotes
- **Headers' job**: Map column names to indexes

### Why This Design is Smart

**1. Flexibility** - Works with any number of columns:
```java
Headers small = new Headers("id", "name");              // 2 columns
Headers medium = new Headers("id", "name", "age", ...); // 10 columns
Headers large = new Headers(...);                       // 100 columns
```

**2. Testability** - Can test without reading files:
```java
@Test
void shouldHandleThreeColumns() {
    Headers headers = new Headers("id", "name", "age");
    assertEquals(3, headers.size());
}
```

**3. Reusability** - Can be used in different contexts:
```java
// From CSV file
Headers fromFile = csvReader.getHeaders();

// From database query
Headers fromDB = new Headers(resultSet.getColumnNames());

// From API response
Headers fromAPI = new Headers(jsonObject.keys());
```

---

## Design Decisions

### Why Two Data Structures?

The Header uses **both** a List and a Map:

```java
private final List<String> columnNames;           // Preserves order
private final Map<String, Integer> nameToIndex;   // Fast lookup
```

#### Analogy: Restaurant Menu

Think of a CSV like a restaurant menu:
- **List** = Menu in order (Appetizers, Mains, Desserts)
- **Map** = Quick reference card ("Burger" → Item #5)

**Why both?**
- **List**: Maintains column order (essential for writing CSV)
- **Map**: Fast name-to-index lookup (essential for reading data)

### Performance Comparison

| Operation | List Only | Map Only | List + Map |
|-----------|-----------|----------|------------|
| Get name by index | O(1) ✅ | O(n) ❌ | O(1) ✅ |
| Get index by name | O(n) ❌ | O(1) ✅ | O(1) ✅ |
| Maintain order | ✅ | ❌ | ✅ |
| Memory usage | Low | Medium | Medium |

**Conclusion**: Using both gives O(1) performance for all operations at the cost of slightly more memory.

---

## User-Friendly Features

### Whitespace Trimming

Headers automatically removes leading and trailing spaces from column names.

**Why?** CSV files often have inconsistent spacing:
```csv
id,  name  , age,city
```

**Without trimming**: `"  name  "` and `"name"` are different columns (confusing!)

**With trimming**: Both become `"name"` (works as expected!)

**Example:**
```java
Headers headers = new Headers(Arrays.asList("  id  ", "name", "  age  "));
headers.getIndex("id");    // Works! Returns 0
headers.getName(0);        // Returns "id" (trimmed)
```

### Case-Insensitive Lookup

You can find columns using any capitalization.

**Why?** Users shouldn't have to remember exact capitalization:
```csv
ID,Name,AGE
```

**Without case-insensitive**: Must use exact case: `getIndex("ID")` ✅ but `getIndex("id")` ❌

**With case-insensitive**: All work: `getIndex("ID")`, `getIndex("id")`, `getIndex("Id")` ✅

**Example:**
```java
Headers headers = new Headers(Arrays.asList("ID", "Name", "AGE"));
headers.getIndex("id");      // Works! Returns 0
headers.getIndex("NAME");    // Works! Returns 1
headers.getIndex("age");     // Works! Returns 2
headers.getName(0);          // Returns "ID" (original case preserved)
```

**Trade-off:** Columns with same name but different case are treated as duplicates:
```java
// This throws exception (duplicate detected)
new Headers(Arrays.asList("id", "ID", "name"));
```

---

## Understanding Big O Notation

### What is O(1) vs O(n)?

**O(1) - Constant Time** (Fast, always the same)
- Like going directly to a numbered mailbox
- Performance doesn't change with data size
- Example: `array[5]` - go straight to position 5

**O(n) - Linear Time** (Slow, grows with data)
- Like searching through every book on a shelf
- Performance doubles when data doubles
- Example: Loop through 1,000 items to find "age"

### Visual Example

```
CSV Columns: 10      100      1,000    10,000
─────────────────────────────────────────────
O(1) operations:  1        1        1        1      ← Always fast!
O(n) operations:  10       100      1,000    10,000 ← Gets slower!
```

**For a CSV with 100 columns and 1,000 rows**:
- Without Map: 100 × 1,000 = 100,000 operations
- With Map: 1 × 1,000 = 1,000 operations
- **100x faster!**

---

## Data Structures Explained

### 1. List<String> - Ordered Collection

```
Index:  0      1        2       3
       ┌────┬──────┬──────┬──────┐
Value: │ id │ name │ age  │ city │
       └────┴──────┴──────┴──────┘
```

**Characteristics**:
- **Maintains insertion order**: Columns stay in sequence
- **Index access**: `list.get(2)` → "age" (instant)
- **Dynamic size**: Can grow/shrink as needed
- **Search by value**: Must loop through all items (slow)

**Use cases**:
- Writing CSV header line in correct order
- Getting column name for error messages
- Iterating through columns sequentially

### 2. Map<String, Integer> - Key-Value Pairs

```
Key (Name)    →    Value (Index)
─────────────────────────────────
"id"          →    0
"name"        →    1
"age"         →    2
"city"        →    3
```

**Characteristics**:
- **Fast lookup**: `map.get("age")` → 2 (instant via hashing)
- **Unique keys**: Each column name appears once
- **No guaranteed order**: Internal organization optimized for speed
- **Reverse lookup slow**: Finding key by value requires looping

**Use cases**:
- User requests: `row.get("age")` → need index quickly
- Schema validation: Check if required columns exist
- Data transformation: Access columns by name

### 3. How Hashing Works (Map's Secret)

**Analogy**: Library with 10 shelves

**Rule**: Books go on shelf based on first letter
- A-B → Shelf 0
- C-D → Shelf 1
- E-F → Shelf 2

**Finding "Dune"**:
1. "D" → Shelf 1 (instant calculation)
2. Go to Shelf 1 (instant)
3. Only a few books there (quick search)

**This is hashing!**
```
"age" → hash function → 7 → store at position 7
```

**Looking up "age"**:
1. Hash "age" → 7 (instant)
2. Go to position 7 (instant)
3. Return value: 2 (instant)

**Result**: O(1) lookup without checking other entries!

---

## Class API

### Constructor

```java
public Headers(List<String> columnNames)
```
**Purpose**: Create Header from a list of column names (from CSV reader)

**Parameters**:
- `columnNames` - Ordered list of column names from CSV first line

**Example**:
```java
List<String> cols = Arrays.asList("id", "name", "age", "city");
Headers headers = new Headers(cols);
```

---

```java
public Headers(String... columnNames)
```
**Purpose**: Create Header from varargs (convenient for testing/manual creation)

**Parameters**:
- `columnNames` - Variable number of column name strings

**Example**:
```java
Headers headers = new Headers("id", "name", "age", "city");
```

---

### Core Methods

```java
public int getIndex(String columnName)
```
**Purpose**: Get the positional index of a column by its name

**Parameters**:
- `columnName` - The name of the column to find

**Returns**: The zero-based index of the column

**Throws**: `IllegalArgumentException` if column doesn't exist

**Performance**: O(1) - uses Map lookup

**Example**:
```java
int index = headers.getIndex("age");  // Returns: 2
String value = rowData[index];       // Get value at position 2
```

**Use case**: When user requests data by column name, translate to array position

---

```java
public String getName(int index)
```
**Purpose**: Get the column name at a specific position

**Parameters**:
- `index` - The zero-based position of the column

**Returns**: The name of the column at that position

**Throws**: `IndexOutOfBoundsException` if index is invalid

**Performance**: O(1) - uses List index access

**Example**:
```java
String colName = headers.getName(2);  // Returns: "age"
System.out.println("Error in column: " + colName);
```

**Use case**: Creating user-friendly error messages with column names instead of numbers

---

```java
public boolean contains(String columnName)
```
**Purpose**: Check if a column exists in the header

**Parameters**:
- `columnName` - The name of the column to check

**Returns**: `true` if column exists, `false` otherwise

**Performance**: O(1) - uses Map containsKey

**Example**:
```java
if (headers.contains("email")) {
    // Process email column
} else {
    // Email column is optional
}
```

**Use case**: Conditional logic based on column presence, validation

---

```java
public int size()
```
**Purpose**: Get the total number of columns

**Returns**: The number of columns in the header

**Performance**: O(1) - returns List size

**Example**:
```java
int numColumns = headers.size();  // Returns: 4
```

**Use case**: Validation (ensure row has correct number of fields), iteration bounds

---

```java
public List<String> getColumnNames()
```
**Purpose**: Get all column names in order

**Returns**: Unmodifiable list of column names in their original order

**Performance**: O(1) - returns reference to internal list (wrapped)

**Example**:
```java
List<String> columns = header.getColumnNames();
// Returns: ["id", "name", "age", "city"]
```

**Use case**: Writing CSV header line, displaying column information, iteration

---

### Validation Methods

```java
public void validate(List<String> requiredColumns)
```
**Purpose**: Ensure all required columns are present in the header

**Parameters**:
- `requiredColumns` - List of column names that must exist

**Throws**: `IllegalArgumentException` with missing column names if validation fails

**Performance**: O(m) where m = number of required columns

**Example**:
```java
List<String> required = Arrays.asList("id", "name", "email");
headers.validate(required);
// Throws exception if "email" is missing
```

**Use case**: Schema validation, ensuring CSV has all necessary columns before processing

---

## Usage Examples

### Example 1: CSV Reader Creating Header

```java
// CsvReader reads first line
String firstLine = "id,name,age,city";
String[] columns = firstLine.split(",");
List<String> columnList = Arrays.asList(columns);

// Create Header
Headers headers = new Headers(columnList);

// Now available for all rows
```

### Example 2: Row Using Header to Get Value

```java
// Row has data and reference to Header
String[] rowData = {"1", "Alice", "25", "Dublin"};
Headers headers = new Headers("id", "name", "age", "city");

// User requests: row.get("age")
int index = headers.getIndex("age");     // 2
String age = rowData[index];            // "25"
```

### Example 3: Schema Validation

```java
Headers headers = new Headers("id", "name", "age");

// Check if required columns exist
List<String> required = Arrays.asList("id", "name", "email");
try {
    headers.validate(required);
} catch (IllegalArgumentException e) {
    System.out.println("Missing columns: " + e.getMessage());
    // Output: "Missing columns: email"
}
```

### Example 4: Writing CSV Header

```java
Headers headers = new Headers("id", "name", "age", "city");

// Write header line to file
StringBuilder line = new StringBuilder();
for (String col : headers.getColumnNames()) {
    line.append(col).append(",");
}
// Output: "id,name,age,city,"
```

### Example 5: Error Messages

```java
// Processing row, error at position 2
try {
    int value = Integer.parseInt(rowData[2]);
} catch (NumberFormatException e) {
    String colName = headers.getName(2);
    System.out.println("Invalid number in column '" + colName + "': " + rowData[2]);
    // Output: "Invalid number in column 'age': abc"
}
```

---

## Design Considerations

### 1. Immutability

**Decision**: Header should be immutable after creation

**Reasoning**:
- CSV structure is fixed after reading first line
- Prevents accidental modifications
- Thread-safe for concurrent access
- Simpler reasoning about state

**Implementation**: Make fields `final`, return unmodifiable collections

---

### 2. Case Sensitivity

**Question**: Should "Name" and "name" be treated as the same column?

**Options**:
- **Case-sensitive** (default): "Name" ≠ "name"
  - Pros: Matches CSV exactly, no ambiguity
  - Cons: User errors ("Name" vs "name")

- **Case-insensitive**: "Name" = "name"
  - Pros: More forgiving, user-friendly
  - Cons: Potential conflicts, more complex

**Recommendation**: Start case-sensitive, add case-insensitive option later if needed

---

### 3. Duplicate Column Names

**Question**: What if CSV has duplicate columns? `"id,name,age,name"`

**Options**:
- **Reject**: Throw exception during construction
  - Pros: Prevents ambiguity
  - Cons: Some CSVs have duplicates

- **Allow**: Use first occurrence, ignore duplicates
  - Pros: More permissive
  - Cons: Silent data loss

**Recommendation**: Reject duplicates with clear error message

---

### 4. Null/Empty Column Names

**Question**: What if column name is null or empty string?

**Options**:
- **Reject**: Throw exception
- **Allow**: Generate default names ("column_0", "column_1")

**Recommendation**: Reject null, allow empty (some CSVs have unnamed columns)

---

## Testing Strategy

### Test Cases Required

**Constructor Tests**:
- ✅ Create from List
- ✅ Create from varargs
- ✅ Empty column list
- ✅ Null column name
- ✅ Duplicate column names

**Lookup Tests**:
- ✅ Get index by valid name
- ✅ Get index by invalid name
- ✅ Get name by valid index
- ✅ Get name by invalid index (negative, too large)

**Utility Tests**:
- ✅ Contains existing column
- ✅ Contains non-existing column
- ✅ Size returns correct count
- ✅ GetColumnNames returns correct order

**Validation Tests**:
- ✅ Validate with all columns present
- ✅ Validate with missing columns
- ✅ Validate with empty required list

**Edge Cases**:
- ✅ Single column
- ✅ Many columns (100+)
- ✅ Column names with special characters
- ✅ Very long column names

---

## Integration with Other Classes

### Row Class
```java
class Row {
    private final Headers headers;
    private final String[] data;
    
    public String get(String columnName) {
        int index = headers.getIndex(columnName);
        return data[index];
    }
}
```

### CsvReader Class
```java
class CsvReader {
    public List<Row> read(File file) {
        String firstLine = readFirstLine(file);
        Headers headers = new Headers(parseColumns(firstLine));
        
        List<Row> rows = new ArrayList<>();
        for (String line : readRemainingLines(file)) {
            rows.add(new Row(header, parseLine(line)));
        }
        return rows;
    }
}
```

### Schema Class
```java
class Schema {
    public void validate(Headers headers) {
        List<String> required = getRequiredColumns();
        headers.validate(required);
    }
}
```

---

## Performance Characteristics

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| Constructor | O(n) | O(n) |
| getIndex() | O(1) | O(1) |
| getName() | O(1) | O(1) |
| contains() | O(1) | O(1) |
| size() | O(1) | O(1) |
| getColumnNames() | O(1) | O(1) |
| validate() | O(m) | O(1) |

Where:
- n = number of columns
- m = number of required columns

**Memory usage**: ~2x column count (List + Map)

---

## Implementation Strategy

### Phase 1: MVP (Minimum Viable Product) - Week 1

**Core functionality only**:
- Single constructor: `Header(List<String>)`
- Basic methods: `getIndex()`, `getName()`, `contains()`, `size()`
- Simple validation in constructor (null check, duplicates)
- Essential tests for core functionality

**Goal**: Get it working and integrated with Row/CsvReader

### Phase 2: Enhanced Features - Week 2 (if time permits)

- Varargs constructor for convenience
- `getColumnNames()` method
- `validate(List<String>)` for schema checking
- Comprehensive test coverage (80%+)
- Better error messages

### Phase 3: Future Enhancements - Post-Project

1. **Case-insensitive mode**
   ```java
   Headers headers = new Headers(columns, CaseSensitivity.IGNORE);
   ```

2. **Column aliases**
   ```java
   header.addAlias("age", "Age");
   header.getIndex("Age");  // Returns same as "age"
   ```

3. **Column metadata**
   ```java
   header.getType("age");  // Returns: FieldType.INTEGER
   ```

4. **Subset operations**
   ```java
   Header subset = header.select("id", "name");
   ```

---

## MVP Implementation Details

### Constructor with Validation

```java
public Headers(List<String> columnNames) {
    // Validate input
    if (columnNames == null || columnNames.isEmpty()) {
        throw new IllegalArgumentException("Column names cannot be null or empty");
    }
    
    // Check for duplicates and nulls
    Set<String> seen = new HashSet<>();
    for (String name : columnNames) {
        if (name == null) {
            throw new IllegalArgumentException("Column name cannot be null");
        }
        if (!seen.add(name)) {
            throw new IllegalArgumentException("Duplicate column name: " + name);
        }
    }
    
    // Build internal structures
    this.columnNames = new ArrayList<>(columnNames);
    this.nameToIndex = new HashMap<>();
    for (int i = 0; i < columnNames.size(); i++) {
        nameToIndex.put(columnNames.get(i), i);
    }
}
```

### Thread-Safe getColumnNames()

```java
public List<String> getColumnNames() {
    return Collections.unmodifiableList(columnNames);
}
```

---

## References

- **Issue**: [#11 - Header](https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/issues/11)
- **Related Classes**: Row (#10), Schema (#12), CsvReader (#1)
- **Java Collections**: [List](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/List.html), [Map](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Map.html)

---

## Questions or Feedback?

If you have questions about the Headers class design or suggestions for improvements, please:
- Comment on [Issue #11](https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/issues/11)
- Reach out on WhatsApp
- Discuss in the next team meeting

---

**Document Status**: Draft  
**Last Updated**: 2025-11-11  
**Next Review**: After implementation and testing
