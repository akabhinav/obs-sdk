package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Provider for OTLP metric exporters.
 */
class OtlpMetricExporterProvider implements ExporterProvider {

    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        try {
            String endpoint = config.getEndpoint() != null ?
                config.getEndpoint() : "http://localhost:4318/v1/metrics";

            Class<?> exporterClass = Class.forName(
                "com.observability.exporters.otlp.OtlpMetricExporter"
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
            throw new RuntimeException("Failed to create OTLP metric exporter", e);
        }
    }
}
