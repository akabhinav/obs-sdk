package com.observability.core;

import com.observability.api.ObservabilitySDK;
import com.observability.api.logs.LogLevel;
import com.observability.api.logs.LoggerProvider;
import com.observability.api.metrics.MetricRegistry;
import com.observability.api.trace.TracerProvider;
import com.observability.core.logs.LoggerProviderImpl;
import com.observability.core.metrics.MetricRegistryImpl;
import com.observability.core.trace.TracerProviderImpl;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main implementation of the Observability SDK.
 * Uses Java 21 virtual threads for async operations.
 */
public final class ObservabilitySDKImpl implements ObservabilitySDK {

    private final MetricRegistry metricRegistry;
    private final TracerProvider tracerProvider;
    private final LoggerProvider loggerProvider;
    private final String serviceName;
    private final String serviceVersion;
    private final ExecutorService virtualThreadExecutor;

    private volatile boolean shutdown = false;

    private ObservabilitySDKImpl(Builder builder) {
        this.metricRegistry = builder.metricRegistry != null ?
            builder.metricRegistry : new MetricRegistryImpl();
        this.tracerProvider = builder.tracerProvider != null ?
            builder.tracerProvider : new TracerProviderImpl();
        this.loggerProvider = builder.loggerProvider != null ?
            builder.loggerProvider : new LoggerProviderImpl(builder.minLogLevel);
        this.serviceName = builder.serviceName;
        this.serviceVersion = builder.serviceVersion;

        // Create virtual thread executor for async operations
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    @Override
    public TracerProvider getTracerProvider() {
        return tracerProvider;
    }

    @Override
    public LoggerProvider getLoggerProvider() {
        return loggerProvider;
    }

    /**
     * Returns the virtual thread executor for async operations.
     */
    public ExecutorService getExecutor() {
        return virtualThreadExecutor;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    @Override
    public void shutdown() {
        if (shutdown) {
            return;
        }

        shutdown = true;

        // Shutdown virtual thread executor
        virtualThreadExecutor.shutdown();
        try {
            if (!virtualThreadExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                virtualThreadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            virtualThreadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Creates a new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating ObservabilitySDK instances.
     */
    public static final class Builder implements ObservabilitySDK.Builder {

        private MetricRegistry metricRegistry;
        private TracerProvider tracerProvider;
        private LoggerProvider loggerProvider;
        private String serviceName = "unknown-service";
        private String serviceVersion = "unknown";
        private LogLevel minLogLevel = LogLevel.INFO;

        private Builder() {
        }

        @Override
        public Builder setMetricRegistry(MetricRegistry metricRegistry) {
            this.metricRegistry = Objects.requireNonNull(metricRegistry);
            return this;
        }

        @Override
        public Builder setTracerProvider(TracerProvider tracerProvider) {
            this.tracerProvider = Objects.requireNonNull(tracerProvider);
            return this;
        }

        @Override
        public Builder setLoggerProvider(LoggerProvider loggerProvider) {
            this.loggerProvider = Objects.requireNonNull(loggerProvider);
            return this;
        }

        @Override
        public Builder setServiceName(String serviceName) {
            this.serviceName = Objects.requireNonNull(serviceName);
            return this;
        }

        @Override
        public Builder setServiceVersion(String serviceVersion) {
            this.serviceVersion = Objects.requireNonNull(serviceVersion);
            return this;
        }

        /**
         * Sets the minimum log level.
         */
        public Builder setMinLogLevel(LogLevel minLogLevel) {
            this.minLogLevel = Objects.requireNonNull(minLogLevel);
            return this;
        }

        @Override
        public ObservabilitySDK build() {
            return new ObservabilitySDKImpl(this);
        }
    }
}
