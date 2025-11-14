package com.observability.api.trace;

/**
 * Provider for creating Tracer instances.
 * Follows factory pattern for tracer creation.
 */
public interface TracerProvider {

    /**
     * Gets or creates a tracer with the specified instrumentation scope.
     *
     * @param instrumentationScopeName the name of the instrumentation scope
     * @return the tracer instance
     */
    Tracer getTracer(String instrumentationScopeName);

    /**
     * Gets or creates a tracer with version information.
     *
     * @param instrumentationScopeName the name of the instrumentation scope
     * @param instrumentationScopeVersion the version of the instrumentation scope
     * @return the tracer instance
     */
    Tracer getTracer(String instrumentationScopeName, String instrumentationScopeVersion);

    /**
     * Returns a no-op tracer provider.
     */
    static TracerProvider noop() {
        return NoopTracerProvider.INSTANCE;
    }
}
