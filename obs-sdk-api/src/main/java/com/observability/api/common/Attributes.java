package com.observability.api.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable key-value attributes for observability data.
 * Uses Java 21 features for clean, type-safe design.
 */
public final class Attributes {

    private final Map<String, Object> attributes;

    private Attributes(Map<String, Object> attributes) {
        this.attributes = Map.copyOf(attributes);
    }

    /**
     * Creates an empty Attributes instance.
     */
    public static Attributes empty() {
        return new Attributes(Map.of());
    }

    /**
     * Creates Attributes from a map.
     */
    public static Attributes of(Map<String, Object> attributes) {
        Objects.requireNonNull(attributes, "attributes cannot be null");
        return new Attributes(attributes);
    }

    /**
     * Creates a builder for constructing Attributes.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Convenience method to create Attributes with a single key-value pair.
     */
    public static Attributes of(String key, Object value) {
        return new Builder().put(key, value).build();
    }

    /**
     * Gets an attribute value by key.
     */
    public Object get(String key) {
        return attributes.get(key);
    }

    /**
     * Gets an attribute value with type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        throw new ClassCastException("Cannot cast " + value.getClass() + " to " + type);
    }

    /**
     * Returns all attributes as an immutable map.
     */
    public Map<String, Object> asMap() {
        return attributes;
    }

    /**
     * Checks if attributes are empty.
     */
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    /**
     * Returns the number of attributes.
     */
    public int size() {
        return attributes.size();
    }

    /**
     * Creates a new Attributes instance by merging with additional attributes.
     */
    public Attributes merge(Attributes other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        Map<String, Object> merged = new HashMap<>(this.attributes);
        merged.putAll(other.attributes);
        return new Attributes(merged);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attributes that = (Attributes) o;
        return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes);
    }

    @Override
    public String toString() {
        return "Attributes" + attributes;
    }

    /**
     * Builder for creating Attributes instances.
     */
    public static final class Builder {
        private final Map<String, Object> attributes = new HashMap<>();

        /**
         * Adds a key-value pair.
         */
        public Builder put(String key, Object value) {
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(value, "value cannot be null");
            attributes.put(key, value);
            return this;
        }

        /**
         * Adds multiple key-value pairs.
         */
        public Builder putAll(Map<String, Object> attributes) {
            Objects.requireNonNull(attributes, "attributes cannot be null");
            attributes.forEach(this::put);
            return this;
        }

        /**
         * Builds the immutable Attributes instance.
         */
        public Attributes build() {
            return new Attributes(attributes);
        }
    }
}
