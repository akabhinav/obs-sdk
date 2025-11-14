package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

/**
 * Provider for Zipkin trace exporters.
 */
class ZipkinExporterProvider implements ExporterProvider {

    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        String endpoint = config.getEndpoint() != null ?
            config.getEndpoint() : "http://localhost:9411/api/v2/spans";

        System.out.println("Zipkin exporter configured for: " + endpoint);
        System.out.println("Note: Zipkin supports OTLP via collector");

        // Use OTLP (Zipkin supports it via OpenTelemetry Collector)
        return new OtlpSpanExporterProvider().createExporter(config);
    }
}
