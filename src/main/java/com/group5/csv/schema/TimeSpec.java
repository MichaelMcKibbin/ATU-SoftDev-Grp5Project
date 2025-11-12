package com.group5.csv.schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A lightweight wrapper around {@link DateTimeSpec} focused on *time-only* values.
 * Keeps schema declarations explicit (FieldType.TIME) while reusing the robust engine.
 *
 * Typical:
 *   TimeSpec spec = new TimeSpec();
 *   LocalTime t = spec.parse("11:59 PM");
 *   String out = spec.format(t);
 */
public class TimeSpec {

    private final DateTimeSpec base;

    /**
     * Default accepts common time formats: ISO_LOCAL_TIME, 24h and 12h with AM/PM.
     * Uses DEFAULTS policy to pair time-only inputs with epoch date for internal LDT.
     */
    public TimeSpec() {
        this(new DateTimeSpec.Builder()
                .acceptPresets("ISO_LOCAL_TIME")
                .acceptPatterns("HH:mm", "HH:mm:ss", "hh:mm a")
                .strict(false)
                .allowBlank(false)
                .missingPartPolicy(DateTimeSpec.MissingPartPolicy.DEFAULTS)
                .build());
    }

    /** Custom base for advanced needs. */
    public TimeSpec(DateTimeSpec base) {
        this.base = base;
    }

    /** Parse as LocalTime; internally coerces through LocalDateTime using policy. */
    public LocalTime parse(String text) {
        LocalDateTime dt = base.parse(text);
        return dt != null ? dt.toLocalTime() : null;
    }

    /** Format using base formatter; pairs with a fixed date to satisfy formatter. */
    public String format(LocalTime value) {
        return base.format(value != null ? value.atDate(LocalDate.of(1970, 1, 1)) : null);
    }

    public boolean isValid(String text) { return base.isValid(text); }
    public DateTimeSpec getBase() { return base; }
}
