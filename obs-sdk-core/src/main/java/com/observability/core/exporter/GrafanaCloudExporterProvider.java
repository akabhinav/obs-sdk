package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

/**
 * Provider for Grafana Cloud exporters.
 */
class GrafanaCloudExporterProvider implements ExporterProvider {

    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        String endpoint = config.getEndpoint() != null ?
            config.getEndpoint() : "https://otlp-gateway-prod-us-central-0.grafana.net/otlp";

        System.out.println("Grafana Cloud exporter configured for: " + endpoint);
        System.out.println("Note: Grafana Cloud supports OTLP natively");

        // Grafana Cloud supports OTLP
        return new OtlpMetricExporterProvider().createExporter(config);
    }
}
