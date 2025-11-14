package com.observability.api.trace;

import java.util.Objects;

/**
 * Immutable span context for trace propagation.
 * Contains trace ID, span ID, and trace flags.
 */
public record SpanContext(
    String traceId,
    String spanId,
    byte traceFlags,
    boolean sampled,
    boolean valid
) {

    public SpanContext {
        Objects.requireNonNull(traceId, "traceId cannot be null");
        Objects.requireNonNull(spanId, "spanId cannot be null");
    }

    /**
     * Returns an invalid span context.
     */
    public static SpanContext invalid() {
        return new SpanContext("", "", (byte) 0, false, false);
    }

    /**
     * Creates a new span context.
     */
    public static SpanContext create(String traceId, String spanId, byte traceFlags, boolean sampled) {
        return new SpanContext(traceId, spanId, traceFlags, sampled, true);
    }

    /**
     * Returns true if this span context is valid.
     */
    public boolean isValid() {
        return valid && !traceId.isEmpty() && !spanId.isEmpty();
    }

    /**
     * Returns true if this span is sampled.
     */
    public boolean isSampled() {
        return sampled;
    }
}
