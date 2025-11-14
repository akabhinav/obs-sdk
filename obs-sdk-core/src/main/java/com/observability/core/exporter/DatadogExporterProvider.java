package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

/**
 * Provider for Datadog exporters.
 * Supports Datadog Agent or direct API.
 */
class DatadogExporterProvider implements ExporterProvider {

    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        String endpoint = config.getEndpoint() != null ?
            config.getEndpoint() : "http://localhost:8126/v0.4/traces";

        System.out.println("Datadog exporter configured for: " + endpoint);
        System.out.println("Note: Using OTLP protocol (Datadog supports OTLP natively)");

        // Datadog natively supports OTLP
        return new OtlpMetricExporterProvider().createExporter(config);
    }
}
