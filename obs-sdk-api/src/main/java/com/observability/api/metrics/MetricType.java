package com.observability.api.metrics;

/**
 * Enum representing different types of metrics.
 * Sealed-like pattern using enum for type safety.
 */
public enum MetricType {
    /**
     * Counter - monotonically increasing value.
     */
    COUNTER,

    /**
     * Gauge - value that can go up or down.
     */
    GAUGE,

    /**
     * Histogram - statistical distribution of values.
     */
    HISTOGRAM,

    /**
     * Summary - similar to histogram with quantiles.
     */
    SUMMARY
}
