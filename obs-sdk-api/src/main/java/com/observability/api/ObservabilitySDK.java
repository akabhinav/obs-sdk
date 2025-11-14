package com.observability.api;

import com.observability.api.logs.LoggerProvider;
import com.observability.api.metrics.MetricRegistry;
import com.observability.api.trace.TracerProvider;

/**
 * Main entry point for the Observability SDK.
 * Provides access to all observability components.
 */
public interface ObservabilitySDK {

    /**
     * Returns the metric registry.
     */
    MetricRegistry getMetricRegistry();

    /**
     * Returns the tracer provider.
     */
    TracerProvider getTracerProvider();

    /**
     * Returns the logger provider.
     */
    LoggerProvider getLoggerProvider();

    /**
     * Shuts down the SDK and releases resources.
     */
    void shutdown();

    /**
     * Returns a builder for creating SDK instances.
     */
    static Builder builder() {
        throw new UnsupportedOperationException(
            "Builder implementation should be provided by obs-sdk-core module"
        );
    }

    /**
     * Builder interface for SDK configuration.
     */
    interface Builder {

        /**
         * Sets the metric registry.
         */
        Builder setMetricRegistry(MetricRegistry metricRegistry);

        /**
         * Sets the tracer provider.
         */
        Builder setTracerProvider(TracerProvider tracerProvider);

        /**
         * Sets the logger provider.
         */
        Builder setLoggerProvider(LoggerProvider loggerProvider);

        /**
         * Sets the service name.
         */
        Builder setServiceName(String serviceName);

        /**
         * Sets the service version.
         */
        Builder setServiceVersion(String serviceVersion);

        /**
         * Builds the SDK instance.
         */
        ObservabilitySDK build();
    }
}
