package com.observability.api.trace;

import com.observability.api.common.Attributes;

import java.time.Instant;

/**
 * Represents a single operation within a trace.
 * Follows builder pattern for flexible span configuration.
 */
public interface Span extends AutoCloseable {

    /**
     * Returns the span context.
     */
    SpanContext getSpanContext();

    /**
     * Sets an attribute on the span.
     */
    Span setAttribute(String key, Object value);

    /**
     * Sets multiple attributes on the span.
     */
    Span setAttributes(Attributes attributes);

    /**
     * Adds an event to the span.
     */
    Span addEvent(String name);

    /**
     * Adds an event with attributes.
     */
    Span addEvent(String name, Attributes attributes);

    /**
     * Adds an event with a timestamp.
     */
    Span addEvent(String name, Attributes attributes, Instant timestamp);

    /**
     * Records an exception on the span.
     */
    Span recordException(Throwable exception);

    /**
     * Records an exception with additional attributes.
     */
    Span recordException(Throwable exception, Attributes attributes);

    /**
     * Sets the status of the span.
     */
    Span setStatus(SpanStatus status);

    /**
     * Sets the status with a description.
     */
    Span setStatus(SpanStatus status, String description);

    /**
     * Updates the span name.
     */
    Span updateName(String name);

    /**
     * Marks the span as ended.
     */
    void end();

    /**
     * Marks the span as ended with a specific timestamp.
     */
    void end(Instant timestamp);

    /**
     * Returns true if the span is recording.
     */
    boolean isRecording();

    /**
     * Closes the span (calls end()).
     */
    @Override
    default void close() {
        end();
    }

    /**
     * Returns a no-op span that does nothing.
     */
    static Span noop() {
        return NoopSpan.INSTANCE;
    }

    /**
     * Status of a span.
     */
    enum SpanStatus {
        /**
         * The operation completed successfully.
         */
        OK,

        /**
         * The operation contains an error.
         */
        ERROR,

        /**
         * The default status.
         */
        UNSET
    }
}
