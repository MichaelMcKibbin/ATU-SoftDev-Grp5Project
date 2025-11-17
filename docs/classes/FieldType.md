# FieldType Enum - Documentation

**Author**: Michael McKibbin (initial), Edson Ferreira (expanded)  
**Date**: November 15, 2025  
**Issue**: [#14 - FieldType](https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/issues/14)  
**Package**: `com.group5.csv.core`

---

## What It Does

Defines supported CSV data types. Each type can parse CSV strings to typed objects and format them back.

```java
Integer age = (Integer) FieldType.INT.parse("25", field);
String csv = FieldType.INT.format(25, field);  // "25"
```

---

## Supported Types

| Type | Java Type | Example |
|------|-----------|---------|
| STRING | String | "hello" |
| INT | Integer | "123" → 123 |
| LONG | Long | "9999999999" → 9999999999L |
| DOUBLE | Double | "123.456" → 123.456 |
| DECIMAL | BigDecimal | "123.45" (uses DecimalSpec) |
| BOOLEAN | Boolean | "true", "1", "yes" → true |
| DATE | LocalDate | "2025-11-15" (uses DateSpec) |
| DATETIME | LocalDateTime | "2025-11-15T10:30" (uses DateTimeSpec) |

---

## How It Works

**Simple types** (STRING, INT, LONG, DOUBLE, BOOLEAN):
```java
FieldType.INT.parse("123", field)  →  Integer(123)
```

**Complex types** (DECIMAL, DATE, DATETIME) use specs:
```java
// DECIMAL uses DecimalSpec for precision/scale
FieldType.DECIMAL.parse("123.456", field)  →  BigDecimal(123.46)

// DATE uses DateSpec for format patterns
FieldType.DATE.parse("2025-11-15", field)  →  LocalDate(2025, 11, 15)
```

---

## Null Handling

All types handle null consistently:
```java
parse(null, field)  →  null
parse("", field)    →  null (empty treated as null)
format(null, field) →  ""
```

---

## Error Handling

Invalid input throws `IllegalArgumentException`:
```java
FieldType.INT.parse("abc", field)  // throws exception
```

---

## Usage Examples

### Basic Types
```java
Field field = new MockField(FieldType.INT);

Integer age = (Integer) FieldType.INT.parse("25", field);
Long id = (Long) FieldType.LONG.parse("123456789", field);
Double price = (Double) FieldType.DOUBLE.parse("19.99", field);
Boolean active = (Boolean) FieldType.BOOLEAN.parse("true", field);
```

### With Specs
```java
// DECIMAL with 2 decimal places
DecimalSpec spec = DecimalSpec.builder().scale(2).build();
Field field = new MockField(FieldType.DECIMAL, spec);
BigDecimal amount = (BigDecimal) FieldType.DECIMAL.parse("123.456", field);
// Result: 123.46 (rounded)

// DATE with default formats
DateSpec dateSpec = new DateSpec();
Field dateField = new MockField(FieldType.DATE, dateSpec);
LocalDate date = (LocalDate) FieldType.DATE.parse("2025-11-15", dateField);
```

---

## Integration

**Used by:**
- Schema - Field type definitions
- RowBuilder - Parse CSV values
- CsvWriter - Format typed values

**Depends on:**
- Field interface - Configuration
- DecimalSpec, DateSpec, DateTimeSpec - Complex type specs

---

## Testing

- Multiple tests covering 8 types
- Tests parse, format, null handling, errors
- Mock Field used until FieldSpec (Issue #13) is ready
- Damn! I underrated the amount of work here, despite the repetitive syntax, it is tiring, hope I didnt miss anything important

---

## Related

- **Field** (#13) - Configuration interface
- **DecimalSpec** - Decimal configuration
- **DateTimeSpec** - DateTime configuration
- **DateSpec** - Date configuration

---

**Updated**: 2025-11-15
