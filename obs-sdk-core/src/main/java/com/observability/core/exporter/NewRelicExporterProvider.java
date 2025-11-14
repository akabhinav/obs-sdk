package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

/**
 * Provider for New Relic exporters.
 */
class NewRelicExporterProvider implements ExporterProvider {

    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        String endpoint = config.getEndpoint() != null ?
            config.getEndpoint() : "https://otlp.nr-data.net:4318";

        System.out.println("New Relic exporter configured for: " + endpoint);
        System.out.println("Note: New Relic supports OTLP natively");

        // New Relic supports OTLP
        return new OtlpMetricExporterProvider().createExporter(config);
    }
}
