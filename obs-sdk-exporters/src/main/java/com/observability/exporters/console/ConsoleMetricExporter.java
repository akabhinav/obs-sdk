package com.observability.exporters.console;

import com.observability.api.exporter.Exporter;
import com.observability.api.exporter.MetricExporter;
import com.observability.api.metrics.Counter;
import com.observability.api.metrics.Gauge;
import com.observability.api.metrics.Histogram;
import com.observability.api.metrics.Metric;

import java.util.Collection;

/**
 * Console exporter for metrics - prints metrics to console.
 * Useful for development and debugging.
 */
public final class ConsoleMetricExporter implements MetricExporter {

    private volatile boolean shutdown = false;

    @Override
    public ExportResult export(Collection<Metric> metrics) {
        if (shutdown) {
            return ExportResult.FAILURE_NOT_RETRYABLE;
        }

        try {
            System.out.println("\n=== Metrics Export ===");
            for (Metric metric : metrics) {
                exportMetric(metric);
            }
            System.out.println("=====================\n");
            return ExportResult.SUCCESS;
        } catch (Exception e) {
            System.err.println("Error exporting metrics: " + e.getMessage());
            return ExportResult.FAILURE_RETRYABLE;
        }
    }

    private void exportMetric(Metric metric) {
        switch (metric.type()) {
            case COUNTER -> exportCounter((Counter) metric);
            case GAUGE -> exportGauge((Gauge) metric);
            case HISTOGRAM -> exportHistogram((Histogram) metric);
        }
    }

    private void exportCounter(Counter counter) {
        System.out.printf("Counter: %s = %.2f %s%n",
            counter.name(),
            counter.value(),
            counter.unit()
        );
        System.out.printf("  Description: %s%n", counter.description());
        if (!counter.attributes().isEmpty()) {
            System.out.printf("  Attributes: %s%n", counter.attributes());
        }
    }

    private void exportGauge(Gauge gauge) {
        System.out.printf("Gauge: %s = %.2f %s%n",
            gauge.name(),
            gauge.value(),
            gauge.unit()
        );
        System.out.printf("  Description: %s%n", gauge.description());
        if (!gauge.attributes().isEmpty()) {
            System.out.printf("  Attributes: %s%n", gauge.attributes());
        }
    }

    private void exportHistogram(Histogram histogram) {
        Histogram.HistogramSnapshot snapshot = histogram.snapshot();

        System.out.printf("Histogram: %s (%s)%n", histogram.name(), histogram.unit());
        System.out.printf("  Description: %s%n", histogram.description());
        System.out.printf("  Count: %d%n", snapshot.count());
        System.out.printf("  Sum: %.2f%n", snapshot.sum());
        System.out.printf("  Min: %.2f%n", snapshot.min());
        System.out.printf("  Max: %.2f%n", snapshot.max());
        System.out.printf("  Mean: %.2f%n", snapshot.mean());

        double[] percentiles = snapshot.percentiles();
        double[] values = snapshot.percentileValues();
        for (int i = 0; i < percentiles.length; i++) {
            System.out.printf("  p%.0f: %.2f%n", percentiles[i], values[i]);
        }

        if (!histogram.attributes().isEmpty()) {
            System.out.printf("  Attributes: %s%n", histogram.attributes());
        }
    }

    @Override
    public ExportResult flush() {
        return ExportResult.SUCCESS;
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }
}
