package com.group5.csv.schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A lightweight wrapper around {@link DateTimeSpec} focused on *date-only* values.
 * Keeps schema declarations explicit (FieldType.DATE) while reusing the robust engine.
 *
 * Typical:
 *   DateSpec spec = new DateSpec();
 *   LocalDate d = spec.parse("2025-11-09");
 *   String out = spec.format(d);
 */
public class DateSpec {

    private final DateTimeSpec base;

    /**
     * Default accepts common date formats (ISO, EU, US) in strict mode.
     */
    public DateSpec() {
        this(new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_DATE", "EU_DMY", "US_MDY")
                .strict(true)
                .allowBlank(false)
                .build());
    }

    /** Custom base for advanced needs. */
    public DateSpec(DateTimeSpec base) {
        this.base = base;
    }

    /** Parse as LocalDate; date-only inputs will map to midnight if needed. */
    public LocalDate parse(String text) {
        LocalDateTime dt = base.parse(text);
        return dt != null ? dt.toLocalDate() : null;
    }

    /** Format using base formatter (first accepted format). */
    public String format(LocalDate value) {
        return base.format(value != null ? value.atStartOfDay() : null);
    }

    public boolean isValid(String text) { return base.isValid(text); }
    public DateTimeSpec getBase() { return base; }
}
