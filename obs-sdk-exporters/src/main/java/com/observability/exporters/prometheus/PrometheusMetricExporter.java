package com.observability.exporters.prometheus;

import com.observability.api.exporter.Exporter;
import com.observability.api.exporter.MetricExporter;
import com.observability.api.metrics.Counter;
import com.observability.api.metrics.Gauge;
import com.observability.api.metrics.Histogram;
import com.observability.api.metrics.Metric;
import com.observability.exporters.http.HttpExporter;

import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.Map;

/**
 * Prometheus metric exporter.
 * Supports both push gateway and exposition format.
 */
public class PrometheusMetricExporter extends HttpExporter implements MetricExporter {

    public PrometheusMetricExporter(String endpoint, Map<String, String> headers, int timeoutMs) {
        super(endpoint, headers, timeoutMs);
    }

    @Override
    public ExportResult export(Collection<Metric> metrics) {
        if (shutdown) {
            return ExportResult.FAILURE_NOT_RETRYABLE;
        }

        try {
            String prometheusFormat = convertToPrometheusFormat(metrics);
            HttpResponse<String> response = sendRequest(prometheusFormat, "text/plain; version=0.0.4");

            if (isSuccessful(response)) {
                return ExportResult.SUCCESS;
            } else if (isRetryable(response)) {
                return ExportResult.FAILURE_RETRYABLE;
            } else {
                System.err.println("Prometheus export failed: " + response.statusCode());
                return ExportResult.FAILURE_NOT_RETRYABLE;
            }
        } catch (Exception e) {
            System.err.println("Error exporting to Prometheus: " + e.getMessage());
            return ExportResult.FAILURE_RETRYABLE;
        }
    }

    /**
     * Converts metrics to Prometheus exposition format.
     */
    private String convertToPrometheusFormat(Collection<Metric> metrics) {
        StringBuilder sb = new StringBuilder();

        for (Metric metric : metrics) {
            String metricName = sanitizeMetricName(metric.name());

            // Add HELP line
            sb.append("# HELP ").append(metricName).append(" ")
                .append(metric.description()).append("\n");

            // Add TYPE line
            String type = switch (metric.type()) {
                case COUNTER -> "counter";
                case GAUGE -> "gauge";
                case HISTOGRAM -> "histogram";
                default -> "untyped";
            };
            sb.append("# TYPE ").append(metricName).append(" ").append(type).append("\n");

            // Add metric value
            switch (metric.type()) {
                case COUNTER -> {
                    Counter counter = (Counter) metric;
                    sb.append(metricName).append(" ").append(counter.value()).append("\n");
                }
                case GAUGE -> {
                    Gauge gauge = (Gauge) metric;
                    sb.append(metricName).append(" ").append(gauge.value()).append("\n");
                }
                case HISTOGRAM -> {
                    Histogram histogram = (Histogram) metric;
                    Histogram.HistogramSnapshot snapshot = histogram.snapshot();

                    // Prometheus histogram format
                    sb.append(metricName).append("_count ").append(snapshot.count()).append("\n");
                    sb.append(metricName).append("_sum ").append(snapshot.sum()).append("\n");

                    // Add quantiles
                    double[] percentiles = snapshot.percentiles();
                    double[] values = snapshot.percentileValues();
                    for (int i = 0; i < percentiles.length; i++) {
                        double quantile = percentiles[i] / 100.0;
                        sb.append(metricName).append("{quantile=\"").append(quantile).append("\"} ")
                            .append(values[i]).append("\n");
                    }
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private String sanitizeMetricName(String name) {
        // Replace invalid characters with underscores
        return name.replaceAll("[^a-zA-Z0-9:_]", "_");
    }

    @Override
    public ExportResult flush() {
        return ExportResult.SUCCESS;
    }

    @Override
    public void shutdown() {
        close();
    }
}
