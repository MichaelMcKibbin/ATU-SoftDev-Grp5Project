package com.group5.csv.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RowBuilderTest {

    @Test
    void buildsRowWhenValueCountMatchesHeaders() {
        Headers headers = new Headers(List.of("id", "name", "age"));
        RowBuilder builder = new RowBuilder(headers)
                .add("1")
                .add("Jim")
                .add("40");

        Row row = builder.build();

        assertEquals("1", row.get(0));
        assertEquals("Jim", row.get("name"));
        assertEquals("40", row.get(2));
    }

    @Test
    void setByIndexAndNameWorks() {
        Headers headers = new Headers(List.of("id", "name"));
        RowBuilder builder = new RowBuilder(headers)
                .set(0, "42")
                .set("name", "Bob");

        Row row = builder.build();

        assertEquals("42", row.get("id"));
        assertEquals("Bob", row.get("name"));
    }

    @Test
    void buildThrowsIfValueCountDoesNotMatch() {
        Headers headers = new Headers(List.of("id", "name"));
        RowBuilder builder = new RowBuilder(headers)
                .add("only-one-value");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void setByIndexOverridesAndFillsMissingSlots() {
        Headers headers = new Headers(List.of("id", "name", "age"));

        RowBuilder builder = new RowBuilder(headers);
        builder.set(0, "10")
                .set(2, "99"); // index 1 left null, but set() should grow the list safely

        // Now fix the missing middle value
        builder.set(1, "Charlie");

        Row row = builder.build();

        assertEquals("10", row.get("id"));
        assertEquals("Charlie", row.get("name"));
        assertEquals("99", row.get("age"));
    }

    @Test
    void setByNameUsesHeaderLookup() {
        Headers headers = new Headers(List.of("id", "name", "country"));

        RowBuilder builder = new RowBuilder(headers);
        builder.set("name", "David")
                .set("id", "7")
                .set("country", "IE");

        Row row = builder.build();

        assertEquals("7", row.get("id"));
        assertEquals("David", row.get("name"));
        assertEquals("IE", row.get("country"));
    }

    @Test
    void buildThrowsWhenValuesTooFew() {
        Headers headers = new Headers(List.of("id", "name"));
        RowBuilder builder = new RowBuilder(headers)
                .add("only-id");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                builder::build,
                "Expected build() to fail when values < headers.size()"
        );

        assertTrue(ex.getMessage().contains("expected 2 values"),
                "Error message should mention expected size");
    }

    @Test
    void buildThrowsWhenValuesTooMany() {
        Headers headers = new Headers(List.of("id"));

        RowBuilder builder = new RowBuilder(headers)
                .add("1")
                .add("extra");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                builder::build,
                "Expected build() to fail when values > headers.size()"
        );

        // Optional: message check depending on your implementation
        assertTrue(ex.getMessage().contains("expected 1 values"),
                "Error message should mention expected size");
    }

    @Test
    void clearAllowsReuseOfBuilderForMultipleRows() {
        Headers headers = new Headers(List.of("id", "name"));
        RowBuilder builder = new RowBuilder(headers);

        // First row
        builder.add("1").add("Jim");
        Row row1 = builder.build();

        assertEquals("1", row1.get("id"));
        assertEquals("Jim", row1.get("name"));

        // Reuse builder for second row
        builder.clear();
        builder.add("2").add("Bob");
        Row row2 = builder.build();

        assertEquals("2", row2.get("id"));
        assertEquals("Bob", row2.get("name"));
    }

    @Test
    void sizeAndIsCompleteReflectCurrentState() {
        Headers headers = new Headers(List.of("id", "name", "age"));
        RowBuilder builder = new RowBuilder(headers);

        assertEquals(0, builder.size());
        assertFalse(builder.isComplete());

        builder.add("1");
        assertEquals(1, builder.size());
        assertFalse(builder.isComplete());

        builder.add("Alice").add("25");
        assertEquals(3, builder.size());
        assertTrue(builder.isComplete());
    }

    @Test
    void constructorRejectsNullHeaders() {
        assertThrows(IllegalArgumentException.class,
                () -> new RowBuilder(null),
                "RowBuilder should reject null headers");
    }

    @Test
    void setIndexOutOfBoundsThrows() {
        Headers headers = new Headers(List.of("id", "name"));

        RowBuilder builder = new RowBuilder(headers);

        assertThrows(IndexOutOfBoundsException.class,
                () -> builder.set(5, "oops"),
                "Setting far beyond headers.size() should fail fast");
    }
}
