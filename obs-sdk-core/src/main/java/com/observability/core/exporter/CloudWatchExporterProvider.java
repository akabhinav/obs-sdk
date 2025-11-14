package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

/**
 * Provider for AWS CloudWatch exporters.
 * Uses AWS SDK or direct API calls.
 */
class CloudWatchExporterProvider implements ExporterProvider {

    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        // Placeholder for AWS CloudWatch integration
        // In production, this would use AWS SDK for CloudWatch
        String endpoint = config.getEndpoint() != null ?
            config.getEndpoint() : "https://monitoring.us-east-1.amazonaws.com";

        System.out.println("CloudWatch exporter configured for: " + endpoint);
        System.out.println("Note: CloudWatch exporter requires AWS SDK. Using OTLP as fallback.");

        // Fallback to OTLP for now
        return new OtlpMetricExporterProvider().createExporter(config);
    }
}
