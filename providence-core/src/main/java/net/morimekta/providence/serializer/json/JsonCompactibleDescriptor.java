package net.morimekta.providence.serializer.json;

/**
 * Marker class and implementation check for if a struct can use the JSON
 * jsonCompact format, using an array of the fields in numeric order.
 * <p>
 * So the message:
 * <pre>{@code
 * {
 *     "first_field": "The first",
 *     "second_field": 12345
 * }
 * }</pre>
 * Becomes:
 * <pre>{@code
 * ["The first", 12345]
 * }</pre>
 *
 * @see JsonCompactible
 */
public interface JsonCompactibleDescriptor {
    /**
     * @return If the message may be compactible.
     */
    default boolean isJsonCompactible() {
        return true;
    }
}
