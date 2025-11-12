package com.group5.csv.schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

public class DecimalSpec {

    /**
     * What DecimalSpec should do - the scope?
     *
     * Parse/format BigDecimal consistently: fixed scale, optional precision, RoundingMode.
     *
     * Handle blank/nullable, min/max range checks.
     *
     * Be used by FieldType.DECIMAL inside Schema/RowBuilder.
     *
     * Validation and typed parsing are part of the DoD for Schema-based mapping
     */

        // config
        private final int scale;                // e.g., 2 for money
        private final Integer precision;        // total digits, nullable => no cap
        private final RoundingMode rounding;    // e.g., HALF_UP
        private final boolean allowBlank;       // treat "" as null
        private final BigDecimal min;           // optional inclusive bounds
        private final BigDecimal max;
        private final Locale locale;            // reserved (if you later add localized parsing)

        private DecimalSpec(Builder b) {
            this.scale = b.scale;
            this.precision = b.precision;
            this.rounding = b.rounding;
            this.allowBlank = b.allowBlank;
            this.min = b.min;
            this.max = b.max;
            this.locale = b.locale;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private int scale = 2;
            private Integer precision = null;
            private RoundingMode rounding = RoundingMode.HALF_UP;
            private boolean allowBlank = false;
            private BigDecimal min = null, max = null;
            private Locale locale = Locale.ROOT;

            public Builder scale(int s){ this.scale = s; return this; }
            public Builder precision(Integer p){ this.precision = p; return this; }
            public Builder rounding(RoundingMode r){ this.rounding = Objects.requireNonNull(r); return this; }
            public Builder allowBlank(boolean v){ this.allowBlank = v; return this; }
            public Builder min(BigDecimal v){ this.min = v; return this; }
            public Builder max(BigDecimal v){ this.max = v; return this; }
            public Builder locale(Locale l){ this.locale = Objects.requireNonNull(l); return this; }
            public DecimalSpec build(){ return new DecimalSpec(this); }
        }

        /** Parse raw cell text -> BigDecimal (or null if allowed blank). */
        public BigDecimal parse(String raw) {
            if (raw == null) return null;
            String s = raw.trim();
            if (s.isEmpty()) {
                if (allowBlank) return null;
                throw new IllegalArgumentException("Blank not allowed");
            }
            // basic normalization; extend later if you add locale-specifics
            BigDecimal v = new BigDecimal(s);
            if (precision != null && v.precision() > precision) {
                throw new IllegalArgumentException("Precision overflow: " + v);
            }
            v = v.setScale(scale, rounding);
            if (min != null && v.compareTo(min) < 0) throw new IllegalArgumentException("Below min: " + v);
            if (max != null && v.compareTo(max) > 0) throw new IllegalArgumentException("Above max: " + v);
            return v;
        }

        /** Format to text for CsvWriter (always scaled). */
        public String format(BigDecimal v) {
            if (v == null) return allowBlank ? "" : "0".repeat(0); // or throw; decide policy
            return v.setScale(scale, rounding).toPlainString();
        }

        // getters (if needed by Field/Schema)
        public int scale() { return scale; }
        public Integer precision() { return precision; }
        public RoundingMode rounding() { return rounding; }
        public boolean allowBlank() { return allowBlank; }
        public BigDecimal min() { return min; }
        public BigDecimal max() { return max; }
    }

