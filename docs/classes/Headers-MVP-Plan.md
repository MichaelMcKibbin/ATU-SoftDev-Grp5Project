# Header Class - MVP Implementation Plan

**Author**: Edson Ferreira  
**Date**: November 11, 2025  
**Based on**: Mentor feedback and team timeline  
**Target**: Week of Nov 11-18

---

## MVP Scope (This Week)

### What We're Building

A simple, working Headers class that:
- Maps column names to indexes
- Validates input (no nulls, no duplicates)
- Provides basic lookup methods
- Integrates with Row and CsvReader

### What We're NOT Building (Yet)

- Multiple constructors (just one for now)
- Advanced validation methods
- Case-insensitive mode
- Column aliases
- Metadata support

---

## Implementation Checklist

### Step 1: Basic Class Structure ✅
```java
package com.group5.csv.core;

import java.util.*;

public class Headers {
    private final List<String> columnNames;
    private final Map<String, Integer> nameToIndex;
    
    // Constructor and methods to follow
}
```

### Step 2: Constructor with Validation
```java
public Header(List<String> columnNames) {
    // 1. Check for null/empty
    // 2. Check for duplicate names
    // 3. Check for null names
    // 4. Build List
    // 5. Build Map
}
```

**Tests needed**:
- ✅ Valid input works
- ✅ Null list throws exception
- ✅ Empty list throws exception
- ✅ Null column name throws exception
- ✅ Duplicate names throw exception

### Step 3: Core Lookup Methods
```java
public int getIndex(String columnName) { }
public String getName(int index) { }
public boolean contains(String columnName) { }
public int size() { }
```

**Tests needed**:
- ✅ getIndex() with valid name
- ✅ getIndex() with invalid name throws exception
- ✅ getName() with valid index
- ✅ getName() with invalid index throws exception
- ✅ contains() returns true for existing column
- ✅ contains() returns false for missing column
- ✅ size() returns correct count

### Step 4: Integration Test
```java
// Simulate CsvReader usage
String[] csvHeader = {"id", "name", "age", "city"};
Headers headers = new Headers(Arrays.asList(csvHeader));

// Simulate Row usage
String[] rowData = {"1", "Alice", "25", "Dublin"};
int ageIndex = header.getIndex("age");
String age = rowData[ageIndex];  // Should be "25"
```

---

## Test Coverage Target

**Minimum**: 80% (project requirement)
**Realistic for MVP**: 85-90%

**Priority tests**:
1. Constructor validation (critical)
2. Basic lookups (critical)
3. Edge cases (important)
4. Integration scenarios (nice to have)

---

## Timeline

**Day 1 (Nov 11)**: ✅ Design documentation
**Day 2 (Nov 12)**: Implement constructor + tests
**Day 3 (Nov 13)**: Implement lookup methods + tests
**Day 4 (Nov 14)**: Integration testing
**Day 5 (Nov 15)**: Code review, refinements
**Day 6-7 (Nov 16-17)**: Buffer for issues

**Target PR submission**: Nov 17

---

## Success Criteria

- [ ] All MVP methods implemented
- [ ] 80%+ test coverage
- [ ] All tests passing
- [ ] Integrates with Row class (coordinate with teammate)
- [ ] Code reviewed by at least one teammate
- [ ] Documentation updated with actual implementation notes

---

## Questions to Answer During Implementation

1. **Empty string column names**: Allow or reject?
   - **Decision**: Allow (some CSVs have unnamed columns)

2. **Case sensitivity**: "Name" vs "name"
   - **Decision**: Case-sensitive (simpler, matches CSV exactly)

3. **Error messages**: How detailed?
   - **Decision**: Include column name in message for debugging

4. **Thread safety**: Do we need it?
   - **Decision**: Yes - use `final` fields and unmodifiable collections

---

## Integration Points

### With CsvReader
```java
// CsvReader creates Header from first line
String firstLine = reader.readLine();
String[] columns = firstLine.split(",");
Headers headers = new Headers(Arrays.asList(columns));
```

### With Row
```java
// Row uses Header to look up columns
public class Row {
    private final Headers headers;
    private final String[] data;
    
    public String get(String columnName) {
        int index = header.getIndex(columnName);
        return data[index];
    }
}
```

### With Schema (future)
```java
// Schema validates required columns exist
schema.validate(header);
```

---

## Notes from Mentor Feedback

1. **Start simple**: Don't over-engineer the first version
2. **Test early**: Get basic functionality working first
3. **Iterate**: Add features incrementally
4. **Document decisions**: Keep track of what you learn

---

## Next Steps After MVP

Once MVP is working and merged:
1. Add varargs constructor for convenience
2. Add `getColumnNames()` method
3. Add `validate(List<String>)` method
4. Improve error messages
5. Add more edge case tests

---

**Status**: Ready to implement  
**Last Updated**: 2025-11-11
