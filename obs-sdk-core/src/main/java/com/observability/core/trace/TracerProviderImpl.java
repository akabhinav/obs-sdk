package com.observability.core.trace;

import com.observability.api.trace.Tracer;
import com.observability.api.trace.TracerProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe implementation of TracerProvider.
 */
public final class TracerProviderImpl implements TracerProvider {

    private final ConcurrentMap<String, Tracer> tracers = new ConcurrentHashMap<>();

    @Override
    public Tracer getTracer(String instrumentationScopeName) {
        return getTracer(instrumentationScopeName, null);
    }

    @Override
    public Tracer getTracer(String instrumentationScopeName, String instrumentationScopeVersion) {
        String key = instrumentationScopeName +
            (instrumentationScopeVersion != null ? ":" + instrumentationScopeVersion : "");

        return tracers.computeIfAbsent(key, k ->
            new TracerImpl(instrumentationScopeName, instrumentationScopeVersion)
        );
    }
}
