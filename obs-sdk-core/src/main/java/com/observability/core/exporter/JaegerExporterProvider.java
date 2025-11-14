package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

/**
 * Provider for Jaeger trace exporters.
 */
class JaegerExporterProvider implements ExporterProvider {

    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        String endpoint = config.getEndpoint() != null ?
            config.getEndpoint() : "http://localhost:14268/api/traces";

        System.out.println("Jaeger exporter configured for: " + endpoint);
        System.out.println("Note: Jaeger supports OTLP natively");

        // Jaeger supports OTLP
        return new OtlpSpanExporterProvider().createExporter(config);
    }
}
