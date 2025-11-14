package com.observability.core;

import com.observability.api.ObservabilitySDK;
import com.observability.api.exporter.LogExporter;
import com.observability.api.exporter.MetricExporter;
import com.observability.api.exporter.SpanExporter;
import com.observability.api.logs.LogLevel;
import com.observability.api.logs.LoggerProvider;
import com.observability.api.metrics.MetricRegistry;
import com.observability.api.trace.TracerProvider;
import com.observability.core.config.ConfigLoader;
import com.observability.core.config.ObservabilityConfig;
import com.observability.core.exporter.ExporterFactory;
import com.observability.core.logs.LoggerProviderImpl;
import com.observability.core.metrics.MetricRegistryImpl;
import com.observability.core.trace.TracerProviderImpl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
    private final List<MetricExporter> metricExporters;
    private final List<SpanExporter> spanExporters;
    private final List<LogExporter> logExporters;

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
        this.metricExporters = new ArrayList<>(builder.metricExporters);
        this.spanExporters = new ArrayList<>(builder.spanExporters);
        this.logExporters = new ArrayList<>(builder.logExporters);

        // Create virtual thread executor for async operations
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        System.out.println("✅ Observability SDK initialized:");
        System.out.println("   Service: " + serviceName + " v" + serviceVersion);
        System.out.println("   Metric Exporters: " + metricExporters.size());
        System.out.println("   Span Exporters: " + spanExporters.size());
        System.out.println("   Log Exporters: " + logExporters.size());
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

        // Shutdown exporters
        metricExporters.forEach(MetricExporter::shutdown);
        spanExporters.forEach(SpanExporter::shutdown);
        logExporters.forEach(LogExporter::shutdown);

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
     * Returns the configured metric exporters.
     */
    public List<MetricExporter> getMetricExporters() {
        return List.copyOf(metricExporters);
    }

    /**
     * Returns the configured span exporters.
     */
    public List<SpanExporter> getSpanExporters() {
        return List.copyOf(spanExporters);
    }

    /**
     * Returns the configured log exporters.
     */
    public List<LogExporter> getLogExporters() {
        return List.copyOf(logExporters);
    }

    /**
     * Creates a new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates an SDK instance from YAML configuration file.
     * Auto-discovers configuration from default locations or specified path.
     */
    public static ObservabilitySDK fromConfig() {
        return fromConfig(null);
    }

    /**
     * Creates an SDK instance from a specific configuration file.
     */
    public static ObservabilitySDK fromConfig(Path configPath) {
        ConfigLoader loader = new ConfigLoader();
        ObservabilityConfig config;

        if (configPath != null) {
            try {
                config = loader.loadConfigFromFile(configPath);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load config from: " + configPath, e);
            }
        } else {
            config = loader.loadConfig();
        }

        return fromConfig(config);
    }

    /**
     * Creates an SDK instance from a configuration object.
     */
    public static ObservabilitySDK fromConfig(ObservabilityConfig config) {
        Builder builder = new Builder();

        // Set service info
        builder.setServiceName(config.getService().getName());
        builder.setServiceVersion(config.getService().getVersion());

        // Set log level
        try {
            LogLevel logLevel = LogLevel.valueOf(config.getLogs().getLevel().toUpperCase());
            builder.setMinLogLevel(logLevel);
        } catch (Exception e) {
            builder.setMinLogLevel(LogLevel.INFO);
        }

        // Create exporter factory
        ExporterFactory factory = new ExporterFactory();

        // Load metric exporters
        for (ObservabilityConfig.ExporterConfig exporterConfig : config.getExporters().getMetrics()) {
            if (exporterConfig.isEnabled()) {
                try {
                    MetricExporter exporter = factory.createMetricExporter(exporterConfig);
                    builder.addMetricExporter(exporter);
                    System.out.println("✓ Loaded metric exporter: " + exporterConfig.getType());
                } catch (Exception e) {
                    System.err.println("✗ Failed to load metric exporter: " + exporterConfig.getType() + " - " + e.getMessage());
                }
            }
        }

        // Load span exporters
        for (ObservabilityConfig.ExporterConfig exporterConfig : config.getExporters().getTraces()) {
            if (exporterConfig.isEnabled()) {
                try {
                    SpanExporter exporter = factory.createSpanExporter(exporterConfig);
                    builder.addSpanExporter(exporter);
                    System.out.println("✓ Loaded span exporter: " + exporterConfig.getType());
                } catch (Exception e) {
                    System.err.println("✗ Failed to load span exporter: " + exporterConfig.getType() + " - " + e.getMessage());
                }
            }
        }

        // Load log exporters
        for (ObservabilityConfig.ExporterConfig exporterConfig : config.getExporters().getLogs()) {
            if (exporterConfig.isEnabled()) {
                try {
                    LogExporter exporter = factory.createLogExporter(exporterConfig);
                    builder.addLogExporter(exporter);
                    System.out.println("✓ Loaded log exporter: " + exporterConfig.getType());
                } catch (Exception e) {
                    System.err.println("✗ Failed to load log exporter: " + exporterConfig.getType() + " - " + e.getMessage());
                }
            }
        }

        return builder.build();
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
        private final List<MetricExporter> metricExporters = new ArrayList<>();
        private final List<SpanExporter> spanExporters = new ArrayList<>();
        private final List<LogExporter> logExporters = new ArrayList<>();

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

        /**
         * Adds a metric exporter.
         */
        public Builder addMetricExporter(MetricExporter exporter) {
            this.metricExporters.add(Objects.requireNonNull(exporter));
            return this;
        }

        /**
         * Adds a span exporter.
         */
        public Builder addSpanExporter(SpanExporter exporter) {
            this.spanExporters.add(Objects.requireNonNull(exporter));
            return this;
        }

        /**
         * Adds a log exporter.
         */
        public Builder addLogExporter(LogExporter exporter) {
            this.logExporters.add(Objects.requireNonNull(exporter));
            return this;
        }

        @Override
        public ObservabilitySDK build() {
            return new ObservabilitySDKImpl(this);
        }
    }
}
