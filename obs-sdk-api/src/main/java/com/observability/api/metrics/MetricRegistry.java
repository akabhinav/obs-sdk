package com.observability.api.metrics;

import com.observability.api.common.Attributes;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Registry for managing and accessing metrics.
 * Central point for metric creation and retrieval.
 */
public interface MetricRegistry {

    /**
     * Creates or retrieves a counter metric.
     *
     * @param name the metric name
     * @param description the metric description
     * @param unit the unit of measurement
     * @return the counter instance
     */
    Counter counter(String name, String description, String unit);

    /**
     * Creates or retrieves a counter with default unit.
     */
    default Counter counter(String name, String description) {
        return counter(name, description, "1");
    }

    /**
     * Creates or retrieves a counter with attributes.
     */
    Counter counter(String name, String description, String unit, Attributes attributes);

    /**
     * Creates or retrieves a gauge metric.
     *
     * @param name the metric name
     * @param description the metric description
     * @param unit the unit of measurement
     * @return the gauge instance
     */
    Gauge gauge(String name, String description, String unit);

    /**
     * Creates or retrieves a gauge with default unit.
     */
    default Gauge gauge(String name, String description) {
        return gauge(name, description, "1");
    }

    /**
     * Creates or retrieves a gauge with attributes.
     */
    Gauge gauge(String name, String description, String unit, Attributes attributes);

    /**
     * Creates or retrieves a callback-based gauge.
     * The supplier is invoked when the metric is collected.
     */
    Gauge gauge(String name, String description, String unit, Supplier<Double> valueSupplier);

    /**
     * Creates or retrieves a histogram metric.
     *
     * @param name the metric name
     * @param description the metric description
     * @param unit the unit of measurement
     * @return the histogram instance
     */
    Histogram histogram(String name, String description, String unit);

    /**
     * Creates or retrieves a histogram with default unit.
     */
    default Histogram histogram(String name, String description) {
        return histogram(name, description, "ms");
    }

    /**
     * Creates or retrieves a histogram with attributes.
     */
    Histogram histogram(String name, String description, String unit, Attributes attributes);

    /**
     * Returns all registered metrics.
     */
    Collection<Metric> getMetrics();

    /**
     * Removes a metric from the registry.
     *
     * @param name the metric name
     * @return true if the metric was removed
     */
    boolean remove(String name);

    /**
     * Clears all metrics from the registry.
     */
    void clear();
}
