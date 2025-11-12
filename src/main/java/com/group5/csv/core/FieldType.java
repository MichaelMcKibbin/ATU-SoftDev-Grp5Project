package com.group5.csv.core;

import com.group5.csv.schema.DecimalSpec;
import java.math.BigDecimal;

public enum FieldType {

    STRING {
        @Override public Object parse(String raw, Field field) {
            return raw;
        }
        @Override public String format(Object value, Field field) {
            return value == null ? "" : value.toString();
        }
    },

    DECIMAL {
        @Override public Object parse(String raw, Field field) {
            DecimalSpec spec = field.decimalSpec(); // ensure Field has this getter
            return spec.parse(raw);
        }
        @Override public String format(Object value, Field field) {
            return field.decimalSpec().format((BigDecimal) value);
        }
    },

    DATETIME {
        @Override public Object parse(String raw, Field field) {
            return field.dateTimeSpec().parse(raw); // if you have DateTimeSpec
        }
        @Override public String format(Object value, Field field) {
            return field.dateTimeSpec().format(value);
        }
    },

    BOOLEAN {
        @Override public Object parse(String raw, Field field) {
            if (raw == null) return null;
            String s = raw.trim().toLowerCase();
            return s.equals("true") || s.equals("1") || s.equals("y") || s.equals("yes");
        }
        @Override public String format(Object value, Field field) {
            if (value == null) return "";
            return ((Boolean) value) ? "true" : "false";
        }
    };

    // Every constant must implement these:
    public abstract Object parse(String raw, Field field);
    public abstract String format(Object value, Field field);
}
