package com.observability.core.exporter;

import com.observability.api.exporter.LogExporter;
import com.observability.api.exporter.MetricExporter;
import com.observability.api.exporter.SpanExporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

import java.util.*;

/**
 * Factory for creating exporters from configuration.
 * Uses service provider interface for extensibility.
 */
public class ExporterFactory {

    private final Map<String, ExporterProvider> metricProviders = new HashMap<>();
    private final Map<String, ExporterProvider> spanProviders = new HashMap<>();
    private final Map<String, ExporterProvider> logProviders = new HashMap<>();

    public ExporterFactory() {
        registerBuiltInProviders();
        discoverProviders();
    }

    /**
     * Registers built-in exporter providers.
     */
    private void registerBuiltInProviders() {
        // Register console exporters
        registerMetricProvider("console", new ConsoleMetricExporterProvider());
        registerSpanProvider("console", new ConsoleSpanExporterProvider());
        registerLogProvider("console", new ConsoleLogExporterProvider());

        // Register OTLP exporters
        registerMetricProvider("otlp", new OtlpMetricExporterProvider());
        registerMetricProvider("otlp-http", new OtlpMetricExporterProvider());
        registerMetricProvider("otlp-grpc", new OtlpMetricExporterProvider());

        registerSpanProvider("otlp", new OtlpSpanExporterProvider());
        registerSpanProvider("otlp-http", new OtlpSpanExporterProvider());
        registerSpanProvider("otlp-grpc", new OtlpSpanExporterProvider());

        registerLogProvider("otlp", new OtlpLogExporterProvider());
        registerLogProvider("otlp-http", new OtlpLogExporterProvider());

        // Register Prometheus
        registerMetricProvider("prometheus", new PrometheusExporterProvider());
        registerMetricProvider("prometheus-pushgateway", new PrometheusExporterProvider());

        // Register cloud providers
        registerMetricProvider("cloudwatch", new CloudWatchExporterProvider());
        registerMetricProvider("datadog", new DatadogExporterProvider());
        registerMetricProvider("newrelic", new NewRelicExporterProvider());
        registerMetricProvider("grafana-cloud", new GrafanaCloudExporterProvider());

        // Register tracing providers
        registerSpanProvider("zipkin", new ZipkinExporterProvider());
        registerSpanProvider("jaeger", new JaegerExporterProvider());
    }

    /**
     * Discovers exporter providers using ServiceLoader.
     */
    private void discoverProviders() {
        // Future: Use ServiceLoader for plugin discovery
        // ServiceLoader<ExporterProvider> loader = ServiceLoader.load(ExporterProvider.class);
        // for (ExporterProvider provider : loader) {
        //     registerProvider(provider);
        // }
    }

    /**
     * Registers a metric exporter provider.
     */
    public void registerMetricProvider(String type, ExporterProvider provider) {
        metricProviders.put(type.toLowerCase(), provider);
    }

    /**
     * Registers a span exporter provider.
     */
    public void registerSpanProvider(String type, ExporterProvider provider) {
        spanProviders.put(type.toLowerCase(), provider);
    }

    /**
     * Registers a log exporter provider.
     */
    public void registerLogProvider(String type, ExporterProvider provider) {
        logProviders.put(type.toLowerCase(), provider);
    }

    /**
     * Creates a metric exporter from configuration.
     */
    public MetricExporter createMetricExporter(ExporterConfig config) {
        String type = config.getType().toLowerCase();
        ExporterProvider provider = metricProviders.get(type);

        if (provider == null) {
            throw new IllegalArgumentException("Unknown metric exporter type: " + type);
        }

        return (MetricExporter) provider.createExporter(config);
    }

    /**
     * Creates a span exporter from configuration.
     */
    public SpanExporter createSpanExporter(ExporterConfig config) {
        String type = config.getType().toLowerCase();
        ExporterProvider provider = spanProviders.get(type);

        if (provider == null) {
            throw new IllegalArgumentException("Unknown span exporter type: " + type);
        }

        return (SpanExporter) provider.createExporter(config);
    }

    /**
     * Creates a log exporter from configuration.
     */
    public LogExporter createLogExporter(ExporterConfig config) {
        String type = config.getType().toLowerCase();
        ExporterProvider provider = logProviders.get(type);

        if (provider == null) {
            throw new IllegalArgumentException("Unknown log exporter type: " + type);
        }

        return (LogExporter) provider.createExporter(config);
    }

    /**
     * Returns all supported metric exporter types.
     */
    public Set<String> getSupportedMetricExporters() {
        return Collections.unmodifiableSet(metricProviders.keySet());
    }

    /**
     * Returns all supported span exporter types.
     */
    public Set<String> getSupportedSpanExporters() {
        return Collections.unmodifiableSet(spanProviders.keySet());
    }

    /**
     * Returns all supported log exporter types.
     */
    public Set<String> getSupportedLogExporters() {
        return Collections.unmodifiableSet(logProviders.keySet());
    }
}
