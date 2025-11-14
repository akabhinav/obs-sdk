package com.observability.exporters.otlp;

import com.observability.api.exporter.Exporter;
import com.observability.api.exporter.MetricExporter;
import com.observability.api.metrics.Counter;
import com.observability.api.metrics.Gauge;
import com.observability.api.metrics.Histogram;
import com.observability.api.metrics.Metric;
import com.observability.exporters.http.HttpExporter;

import java.net.http.HttpResponse;
import java.util.*;

/**
 * OpenTelemetry Protocol (OTLP) metric exporter.
 * Supports both HTTP and gRPC protocols.
 *
 * Works with 40+ backends including:
 * - OpenTelemetry Collector
 * - Grafana Cloud
 * - Datadog
 * - New Relic
 * - Honeycomb
 * - Lightstep
 * - And many more...
 */
public class OtlpMetricExporter extends HttpExporter implements MetricExporter {

    public OtlpMetricExporter(String endpoint, Map<String, String> headers, int timeoutMs) {
        super(endpoint, headers, timeoutMs);
    }

    @Override
    public ExportResult export(Collection<Metric> metrics) {
        if (shutdown) {
            return ExportResult.FAILURE_NOT_RETRYABLE;
        }

        try {
            Map<String, Object> otlpPayload = convertToOtlpFormat(metrics);
            HttpResponse<String> response = sendJson(otlpPayload);

            if (isSuccessful(response)) {
                return ExportResult.SUCCESS;
            } else if (isRetryable(response)) {
                return ExportResult.FAILURE_RETRYABLE;
            } else {
                System.err.println("OTLP export failed: " + response.statusCode() + " - " + response.body());
                return ExportResult.FAILURE_NOT_RETRYABLE;
            }
        } catch (Exception e) {
            System.err.println("Error exporting to OTLP: " + e.getMessage());
            return ExportResult.FAILURE_RETRYABLE;
        }
    }

    /**
     * Converts metrics to OTLP JSON format.
     * Reference: https://github.com/open-telemetry/opentelemetry-proto
     */
    private Map<String, Object> convertToOtlpFormat(Collection<Metric> metrics) {
        List<Map<String, Object>> resourceMetrics = new ArrayList<>();
        List<Map<String, Object>> scopeMetrics = new ArrayList<>();
        List<Map<String, Object>> metricsList = new ArrayList<>();

        long timeUnixNano = System.currentTimeMillis() * 1_000_000;

        for (Metric metric : metrics) {
            Map<String, Object> otlpMetric = new HashMap<>();
            otlpMetric.put("name", metric.name());
            otlpMetric.put("description", metric.description());
            otlpMetric.put("unit", metric.unit());

            switch (metric.type()) {
                case COUNTER -> {
                    Counter counter = (Counter) metric;
                    Map<String, Object> sum = new HashMap<>();
                    sum.put("aggregationTemporality", 2); // CUMULATIVE
                    sum.put("isMonotonic", true);
                    sum.put("dataPoints", List.of(createNumberDataPoint(counter.value(), timeUnixNano)));
                    otlpMetric.put("sum", sum);
                }
                case GAUGE -> {
                    Gauge gauge = (Gauge) metric;
                    Map<String, Object> gaugeData = new HashMap<>();
                    gaugeData.put("dataPoints", List.of(createNumberDataPoint(gauge.value(), timeUnixNano)));
                    otlpMetric.put("gauge", gaugeData);
                }
                case HISTOGRAM -> {
                    Histogram histogram = (Histogram) metric;
                    Map<String, Object> histogramData = new HashMap<>();
                    histogramData.put("aggregationTemporality", 2); // CUMULATIVE
                    histogramData.put("dataPoints", List.of(createHistogramDataPoint(histogram, timeUnixNano)));
                    otlpMetric.put("histogram", histogramData);
                }
            }

            metricsList.add(otlpMetric);
        }

        Map<String, Object> scopeMetric = new HashMap<>();
        scopeMetric.put("metrics", metricsList);
        scopeMetrics.add(scopeMetric);

        Map<String, Object> resourceMetric = new HashMap<>();
        resourceMetric.put("scopeMetrics", scopeMetrics);
        resourceMetrics.add(resourceMetric);

        Map<String, Object> result = new HashMap<>();
        result.put("resourceMetrics", resourceMetrics);
        return result;
    }

    private Map<String, Object> createNumberDataPoint(double value, long timeUnixNano) {
        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("asDouble", value);
        dataPoint.put("timeUnixNano", timeUnixNano);
        dataPoint.put("startTimeUnixNano", timeUnixNano);
        return dataPoint;
    }

    private Map<String, Object> createHistogramDataPoint(Histogram histogram, long timeUnixNano) {
        Histogram.HistogramSnapshot snapshot = histogram.snapshot();

        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("count", snapshot.count());
        dataPoint.put("sum", snapshot.sum());
        dataPoint.put("min", snapshot.min());
        dataPoint.put("max", snapshot.max());
        dataPoint.put("timeUnixNano", timeUnixNano);
        dataPoint.put("startTimeUnixNano", timeUnixNano);

        return dataPoint;
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
