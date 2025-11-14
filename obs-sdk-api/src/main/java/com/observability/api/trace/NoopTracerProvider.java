package com.observability.api.trace;

/**
 * No-op implementation of TracerProvider.
 */
final class NoopTracerProvider implements TracerProvider {

    static final NoopTracerProvider INSTANCE = new NoopTracerProvider();

    private NoopTracerProvider() {
    }

    @Override
    public Tracer getTracer(String instrumentationScopeName) {
        return NoopTracer.INSTANCE;
    }

    @Override
    public Tracer getTracer(String instrumentationScopeName, String instrumentationScopeVersion) {
        return NoopTracer.INSTANCE;
    }
}
