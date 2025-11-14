package com.observability.api.metrics;

import com.observability.api.common.Attributes;

/**
 * Base interface for all metric types.
 * Follows interface segregation principle for clean API.
 */
public sealed interface Metric permits Counter, Gauge, Histogram {

    /**
     * Returns the name of the metric.
     */
    String name();

    /**
     * Returns the description of the metric.
     */
    String description();

    /**
     * Returns the unit of measurement.
     */
    String unit();

    /**
     * Returns the type of the metric.
     */
    MetricType type();

    /**
     * Returns the attributes associated with this metric.
     */
    Attributes attributes();
}
