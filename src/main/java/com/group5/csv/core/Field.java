package com.group5.csv.core;

import com.group5.csv.schema.DecimalSpec;
import com.group5.csv.schema.DateTimeSpec;
import com.group5.csv.schema.DateSpec;

public interface Field {
    FieldType type();

    // Only required for matching types; others can return null or throw if misused
    default DecimalSpec decimalSpec() { throw new UnsupportedOperationException(); }
    default DateTimeSpec dateTimeSpec() { throw new UnsupportedOperationException(); }
    default DateSpec dateSpec() { throw new UnsupportedOperationException(); }
}