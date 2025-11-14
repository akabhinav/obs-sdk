package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Provider for Prometheus metric exporters.
 */
class PrometheusExporterProvider implements ExporterProvider {

    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        try {
            String endpoint = config.getEndpoint() != null ?
                config.getEndpoint() : "http://localhost:9091/metrics/job/observability-sdk";

            Class<?> exporterClass = Class.forName(
                "com.observability.exporters.prometheus.PrometheusMetricExporter"
            );

            Constructor<?> constructor = exporterClass.getConstructor(
                String.class, Map.class, int.class
            );

            return (Exporter<?>) constructor.newInstance(
                endpoint,
                config.getHeaders(),
                config.getTimeoutMs()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Prometheus exporter", e);
        }
    }
}
