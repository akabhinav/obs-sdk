package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

/**
 * Provider for console metric exporters.
 */
class ConsoleMetricExporterProvider implements ExporterProvider {

    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        try {
            // Load console exporter from exporters module
            Class<?> exporterClass = Class.forName(
                "com.observability.exporters.console.ConsoleMetricExporter"
            );
            return (Exporter<?>) exporterClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create console metric exporter", e);
        }
    }
}
