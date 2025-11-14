package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

/**
 * Provider for console span exporters.
 */
class ConsoleSpanExporterProvider implements ExporterProvider {

    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        try {
            Class<?> exporterClass = Class.forName(
                "com.observability.exporters.console.ConsoleSpanExporter"
            );
            return (Exporter<?>) exporterClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create console span exporter", e);
        }
    }
}
