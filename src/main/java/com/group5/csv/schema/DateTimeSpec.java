package com.group5.csv.schema;

import com.group5.csv.exceptions.ParseException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A multi-format date/time spec that can parse/format with:
 *  - multiple explicit patterns, and/or
 *  - named presets (ISO/RFC/common regional formats).
 *
 * Usage examples:
 *   DateTimeSpec isoOnly = new DateTimeSpec.Builder()
 *       .acceptPresets("ISO_LOCAL_DATE_TIME", "ISO_OFFSET_DATE_TIME")
 *       .build();
 *
 *   DateTimeSpec worldCommon = new DateTimeSpec.Builder()
 *       .acceptPresets("EU_DMY", "US_MDY", "YMD_FLEX", "RFC_1123_DATE_TIME", "ISO_INSTANT")
 *       .acceptPatterns("dd-MMM-uuuu HH:mm", "uuuuMMdd HHmmss")
 *       .strict(false)
 *       .allowBlank(true)
 *       .zone(ZoneId.of("Europe/Dublin"))
 *       .missingPartPolicy(DateTimeSpec.MissingPartPolicy.DEFAULTS)
 *       .build();
 *
 * Design note:
 * We use 'uuuu' (proleptic year) instead of 'yyyy'. In java.time, 'yyyy' is year-of-era
 * and can behave incorrectly around era boundaries. 'uuuu' gives a continuous ISO-8601
 * aligned year numbering and is the recommended modern pattern.
 */
public class DateTimeSpec {

    public enum MissingPartPolicy {
        /** If only a date is present, assume midnight; if only a time is present, use epoch date (1970-01-01). */
        DEFAULTS,
        /** Require full date and time (throw if either part is missing). */
        REQUIRE_FULL
    }

    private final List<DateTimeFormatter> formatters;
    private final Locale locale;
    private final ZoneId zone;
    private final boolean strict;
    private final boolean allowBlank;
    private final MissingPartPolicy missingPartPolicy;

    private DateTimeSpec(List<DateTimeFormatter> formatters,
                         Locale locale,
                         ZoneId zone,
                         boolean strict,
                         boolean allowBlank,
                         MissingPartPolicy missingPartPolicy) {
        this.formatters = List.copyOf(formatters);
        this.locale = locale;
        this.zone = zone;
        this.strict = strict;
        this.allowBlank = allowBlank;
        this.missingPartPolicy = missingPartPolicy;
    }

    /** Try to parse into LocalDateTime. Accepts date-only or time-only input depending on policy. */
    public LocalDateTime parse(String text) {
        if (text == null || text.isBlank()) {
            if (allowBlank) return null;
            throw new ParseException("Blank date/time value not allowed");
        }
        final String s = text.trim();

        for (DateTimeFormatter baseFmt : formatters) {
            DateTimeFormatter fmt = (zone == null) ? baseFmt : baseFmt.withZone(zone);
            try {
                Object parsed = fmt.parseBest(
                        s,
                        LocalDateTime::from,
                        ZonedDateTime::from,
                        OffsetDateTime::from,
                        LocalDate::from,
                        LocalTime::from,
                        Instant::from
                );
                return coerceAfterParse(parsed, s);
            } catch (DateTimeParseException e) {
                if (!strict) {
                    // In lenient mode allow a small normalization pass (e.g., separators).
                    try {
                        String normalized = s.replace('/', '-');
                        Object parsed = fmt.parseBest(
                                normalized,
                                LocalDateTime::from,
                                ZonedDateTime::from,
                                OffsetDateTime::from,
                                LocalDate::from,
                                LocalTime::from,
                                Instant::from
                        );
                        return coerceAfterParse(parsed, s);
                    } catch (Exception ignored) {
                        // fall through to next formatter
                    }
                }
                // try next formatter
            }
        }
        throw new ParseException("No acceptable date/time format matched: " + s);
    }

    /** Formats using the first formatter (write-consistency). Returns "" if null and blanks allowed. */
    public String format(LocalDateTime value) {
        if (value == null) return allowBlank ? "" : null;
        DateTimeFormatter fmt = formatters.get(0);
        return fmt.format(value);
    }

    public boolean isValid(String text) {
        try { parse(text); return true; } catch (Exception e) { return false; }
    }

    public Locale getLocale() { return locale; }
    public ZoneId getZone() { return zone; }
    public boolean isStrict() { return strict; }
    public boolean isAllowBlank() { return allowBlank; }
    public MissingPartPolicy getMissingPartPolicy() { return missingPartPolicy; }
    public List<String> getAcceptedPatterns() {
        return formatters.stream().map(Object::toString).collect(Collectors.toList());
    }

    private LocalDateTime coerceAfterParse(Object parsed, String original) {
        if (parsed instanceof LocalDateTime ldt) return ldt;
        if (parsed instanceof ZonedDateTime zdt) return zdt.toLocalDateTime();
        if (parsed instanceof OffsetDateTime odt) return odt.toLocalDateTime();
        if (parsed instanceof LocalDate ld) {
            if (missingPartPolicy == MissingPartPolicy.DEFAULTS) return ld.atStartOfDay();
            throw new ParseException("Time component required but missing: " + original);
        }
        if (parsed instanceof LocalTime lt) {
            if (missingPartPolicy == MissingPartPolicy.DEFAULTS)
                return LocalDate.of(1970, 1, 1).atTime(lt);
            throw new ParseException("Date component required but missing: " + original);
        }
        if (parsed instanceof Instant inst) {
            ZoneId z = (zone != null) ? zone : ZoneId.systemDefault();
            return LocalDateTime.ofInstant(inst, z);
        }
        throw new ParseException("Could not coerce parsed value for: " + original);
    }

    // ----------------- Builder -----------------

    public static class Builder {
        private final List<DateTimeFormatter> fmts = new ArrayList<>();
        private Locale locale = Locale.ENGLISH;
        private ZoneId zone = null; // use system default at parse time if null
        private boolean strict = true;
        private boolean allowBlank = false;
        private MissingPartPolicy missingPartPolicy = MissingPartPolicy.DEFAULTS;

        /** Add explicit pattern(s). Supports optional sections like [XXX] for timezone. */
        public Builder acceptPatterns(String... patterns) {
            for (String p : patterns) {
                if (p == null || p.isBlank()) continue;
                fmts.add(buildFormatter(p, locale, strict));
            }
            return this;
        }

        /** Add named preset(s): e.g., ISO_LOCAL_DATE_TIME, RFC_1123_DATE_TIME, EU_DMY, US_MDY, ISO_INSTANT, YMD_FLEX. */
        public Builder acceptPresets(String... presetNames) {
            for (String name : presetNames) {
                DateTimeFormatter f = Presets.byName(name, locale, strict);
                if (f != null) fmts.add(f);
                else throw new IllegalArgumentException("Unknown date/time preset: " + name);
            }
            return this;
        }

        public Builder locale(Locale locale) { this.locale = Objects.requireNonNull(locale); return this; }
        public Builder zone(ZoneId zone) { this.zone = zone; return this; }
        public Builder strict(boolean strict) { this.strict = strict; return this; }
        public Builder allowBlank(boolean allowBlank) { this.allowBlank = allowBlank; return this; }
        public Builder missingPartPolicy(MissingPartPolicy p) { this.missingPartPolicy = p; return this; }

        public DateTimeSpec build() {
            if (fmts.isEmpty()) {
                // Sensible defaults: accept common ISO-like forms
                fmts.add(buildFormatter("uuuu-MM-dd['T'HH[:mm][:ss][.SSS][XXX]]", locale, strict));
                fmts.add(Presets.byName("ISO_LOCAL_DATE_TIME", locale, strict));
                fmts.add(Presets.byName("ISO_LOCAL_DATE", locale, strict));
                fmts.add(Presets.byName("ISO_OFFSET_DATE_TIME", locale, strict));
            }
            return new DateTimeSpec(fmts, locale, zone, strict, allowBlank, missingPartPolicy);
        }

        private static DateTimeFormatter buildFormatter(String pattern, Locale locale, boolean strict) {
            DateTimeFormatterBuilder b = new DateTimeFormatterBuilder().parseCaseInsensitive();
            if (!strict) b.parseLenient();
            // Note: We prefer 'uuuu' over 'yyyy' for proleptic year semantics.
            b.appendPattern(pattern);
            return b.toFormatter(locale);
        }
    }

    // ----------------- Presets -----------------

    public static final class Presets {
        private Presets() {}

        public static DateTimeFormatter byName(String name, Locale locale, boolean strict) {
            String key = Objects.requireNonNull(name).trim().toUpperCase(Locale.ROOT);

            switch (key) {
                // Java built-ins
                case "ISO_LOCAL_DATE": return DateTimeFormatter.ISO_LOCAL_DATE.withLocale(locale);
                case "ISO_LOCAL_TIME": return DateTimeFormatter.ISO_LOCAL_TIME.withLocale(locale);
                case "ISO_LOCAL_DATE_TIME": return DateTimeFormatter.ISO_LOCAL_DATE_TIME.withLocale(locale);
                case "ISO_OFFSET_DATE_TIME": return DateTimeFormatter.ISO_OFFSET_DATE_TIME.withLocale(locale);
                case "ISO_ZONED_DATE_TIME": return DateTimeFormatter.ISO_ZONED_DATE_TIME.withLocale(locale);
                case "ISO_INSTANT": return DateTimeFormatter.ISO_INSTANT.withLocale(locale);
                case "RFC_1123_DATE_TIME": return DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(locale);
                default:
                    return regionalPreset(key, locale, strict);
            }
        }

        private static DateTimeFormatter regionalPreset(String key, Locale locale, boolean strict) {
            // EU: 31/12/2025 or 31/12/2025 23:59[:ss][.SSS]
            if (key.equals("EU_DMY")) {
                DateTimeFormatterBuilder b = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive();
                if (!strict) b.parseLenient();
                b.appendPattern("dd/MM/uuuu[' 'HH[:mm][:ss][.SSS]]");
                return b.toFormatter(locale);
            }
            // US: 12/31/2025 or 12/31/2025 11:59 PM (12h) or 23:59 (24h)
            if (key.equals("US_MDY")) {
                DateTimeFormatterBuilder b = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive();
                if (!strict) b.parseLenient();
                b.appendPattern("MM/dd/uuuu")
                        .optionalStart().appendLiteral(' ')
                        .optionalStart().appendPattern("hh[:mm][:ss][.SSS] a").optionalEnd()
                        .optionalStart().appendPattern("HH[:mm][:ss][.SSS]").optionalEnd()
                        .optionalEnd();
                return b.toFormatter(locale);
            }
            // Year-first flexible: 2025-12-31 or 2025-12-31 23:59[:ss][.SSS][XXX]
            if (key.equals("YMD_FLEX")) {
                DateTimeFormatterBuilder b = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive();
                if (!strict) b.parseLenient();
                b.appendPattern("uuuu-MM-dd['T'HH[:mm][:ss][.SSS][XXX]]");
                return b.toFormatter(locale);
            }
            return null;
        }
    }

    // ----------------- Convenience factories (optional) -----------------

    /** Convenience factory mirroring older style new DateTimeSpec("pattern"). */
    public static DateTimeSpec ofPattern(String pattern) {
        return new Builder().acceptPatterns(pattern).build();
    }

    public static DateTimeSpec ofPresets(String... presets) {
        return new Builder().acceptPresets(presets).build();
    }
}
