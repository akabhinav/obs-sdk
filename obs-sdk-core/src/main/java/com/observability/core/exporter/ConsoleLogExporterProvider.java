package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

/**
 * Provider for console log exporters.
 */
class ConsoleLogExporterProvider implements ExporterProvider {

    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        try {
            Class<?> exporterClass = Class.forName(
                "com.observability.exporters.console.ConsoleLogExporter"
            );
            return (Exporter<?>) exporterClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create console log exporter", e);
        }
    }
}
