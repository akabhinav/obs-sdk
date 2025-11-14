package com.observability.api.metrics;

import com.observability.api.common.Attributes;

/**
 * Gauge metric - value that can increase or decrease.
 * Represents a point-in-time value.
 */
public non-sealed interface Gauge extends Metric {

    /**
     * Sets the gauge to a specific value.
     *
     * @param value the value to set
     */
    void set(double value);

    /**
     * Sets the gauge value with additional attributes.
     */
    void set(double value, Attributes attributes);

    /**
     * Increments the gauge by the specified amount.
     */
    void increment(double amount);

    /**
     * Decrements the gauge by the specified amount.
     */
    void decrement(double amount);

    /**
     * Returns the current value of the gauge.
     */
    double value();

    @Override
    default MetricType type() {
        return MetricType.GAUGE;
    }
}
