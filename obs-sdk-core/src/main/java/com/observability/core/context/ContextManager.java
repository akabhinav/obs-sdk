package com.observability.core.context;

import com.observability.api.common.ObservabilityContext;
import com.observability.api.trace.Span;
import com.observability.api.trace.SpanContext;

import java.util.concurrent.Callable;

/**
 * Context manager using Java 21's ScopedValue for thread-safe context propagation.
 * Automatically works with virtual threads.
 */
public final class ContextManager {

    private static final ScopedValue<ObservabilityContext> OBSERVABILITY_CONTEXT =
        ScopedValue.newInstance();

    private static final ScopedValue<Span> CURRENT_SPAN =
        ScopedValue.newInstance();

    private ContextManager() {
    }

    /**
     * Gets the current observability context.
     */
    public static ObservabilityContext getCurrentContext() {
        return OBSERVABILITY_CONTEXT.orElse(ObservabilityContext.empty());
    }

    /**
     * Gets the current span.
     */
    public static Span getCurrentSpan() {
        return CURRENT_SPAN.orElse(Span.noop());
    }

    /**
     * Gets the current span context.
     */
    public static SpanContext getCurrentSpanContext() {
        return getCurrentSpan().getSpanContext();
    }

    /**
     * Executes a runnable with the given context.
     */
    public static void runWithContext(ObservabilityContext context, Runnable runnable) {
        ScopedValue.where(OBSERVABILITY_CONTEXT, context).run(runnable);
    }

    /**
     * Executes a callable with the given context.
     */
    public static <T> T callWithContext(ObservabilityContext context, Callable<T> callable)
        throws Exception {
        return ScopedValue.where(OBSERVABILITY_CONTEXT, context).call(callable);
    }

    /**
     * Executes a runnable with the given span.
     */
    public static void runWithSpan(Span span, Runnable runnable) {
        ScopedValue.where(CURRENT_SPAN, span).run(runnable);
    }

    /**
     * Executes a callable with the given span.
     */
    public static <T> T callWithSpan(Span span, Callable<T> callable) throws Exception {
        return ScopedValue.where(CURRENT_SPAN, span).call(callable);
    }

    /**
     * Executes a runnable with both context and span.
     */
    public static void runWithContextAndSpan(
        ObservabilityContext context,
        Span span,
        Runnable runnable
    ) {
        ScopedValue.where(OBSERVABILITY_CONTEXT, context)
            .where(CURRENT_SPAN, span)
            .run(runnable);
    }

    /**
     * Executes a callable with both context and span.
     */
    public static <T> T callWithContextAndSpan(
        ObservabilityContext context,
        Span span,
        Callable<T> callable
    ) throws Exception {
        return ScopedValue.where(OBSERVABILITY_CONTEXT, context)
            .where(CURRENT_SPAN, span)
            .call(callable);
    }
}
