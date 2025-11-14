package com.observability.api.trace;

import com.observability.api.common.Attributes;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Interface for creating and managing spans.
 * Provides fluent API for tracing operations.
 */
public interface Tracer {

    /**
     * Starts a new span builder.
     */
    SpanBuilder spanBuilder(String spanName);

    /**
     * Creates a span and executes a runnable within it.
     */
    default void withSpan(String spanName, Runnable runnable) {
        try (Span span = spanBuilder(spanName).startSpan()) {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException("Error in span: " + spanName, e);
        }
    }

    /**
     * Creates a span and executes a callable within it.
     */
    default <T> T withSpan(String spanName, Callable<T> callable) throws Exception {
        try (Span span = spanBuilder(spanName).startSpan()) {
            return callable.call();
        }
    }

    /**
     * Creates a span, provides it to a consumer, and executes a runnable.
     */
    default void withSpan(String spanName, Consumer<Span> spanConsumer, Runnable runnable) {
        try (Span span = spanBuilder(spanName).startSpan()) {
            spanConsumer.accept(span);
            runnable.run();
        }
    }

    /**
     * Builder for creating spans with configuration.
     */
    interface SpanBuilder {

        /**
         * Sets the parent span context.
         */
        SpanBuilder setParent(SpanContext parentContext);

        /**
         * Sets the span kind.
         */
        SpanBuilder setSpanKind(SpanKind spanKind);

        /**
         * Sets an attribute on the span.
         */
        SpanBuilder setAttribute(String key, Object value);

        /**
         * Sets multiple attributes.
         */
        SpanBuilder setAttributes(Attributes attributes);

        /**
         * Sets whether the span should be recorded.
         */
        SpanBuilder setNoParent();

        /**
         * Starts the span.
         */
        Span startSpan();
    }
}
