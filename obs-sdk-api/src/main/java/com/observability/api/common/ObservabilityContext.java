package com.observability.api.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Context for propagating observability data across thread boundaries.
 * Immutable and thread-safe design for enterprise use.
 *
 * Uses Java 21's enhanced pattern matching for clean context handling.
 */
public final class ObservabilityContext {

    private static final ObservabilityContext EMPTY = new ObservabilityContext(Map.of());

    private final Map<String, Object> data;

    private ObservabilityContext(Map<String, Object> data) {
        this.data = Map.copyOf(data);
    }

    /**
     * Returns an empty context.
     */
    public static ObservabilityContext empty() {
        return EMPTY;
    }

    /**
     * Creates a new context with a single key-value pair.
     */
    public static ObservabilityContext of(String key, Object value) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        return new ObservabilityContext(Map.of(key, value));
    }

    /**
     * Gets a value from the context.
     */
    public Optional<Object> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    /**
     * Gets a typed value from the context.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        return get(key)
            .filter(type::isInstance)
            .map(value -> (T) value);
    }

    /**
     * Creates a new context by adding a key-value pair.
     */
    public ObservabilityContext with(String key, Object value) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        Map<String, Object> newData = new HashMap<>(this.data);
        newData.put(key, value);
        return new ObservabilityContext(newData);
    }

    /**
     * Creates a new context by merging with another context.
     */
    public ObservabilityContext merge(ObservabilityContext other) {
        if (other == null || other.isEmpty()) {
            return this;
        }

        Map<String, Object> merged = new HashMap<>(this.data);
        merged.putAll(other.data);
        return new ObservabilityContext(merged);
    }

    /**
     * Checks if the context is empty.
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Returns all context data as an immutable map.
     */
    public Map<String, Object> asMap() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObservabilityContext that = (ObservabilityContext) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "ObservabilityContext" + data;
    }
}
