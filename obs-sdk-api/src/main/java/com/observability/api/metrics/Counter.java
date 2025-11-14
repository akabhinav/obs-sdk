package com.observability.api.metrics;

import com.observability.api.common.Attributes;

/**
 * Counter metric - monotonically increasing value.
 * Thread-safe and optimized for high-throughput scenarios.
 */
public non-sealed interface Counter extends Metric {

    /**
     * Increments the counter by 1.
     */
    default void increment() {
        increment(1.0);
    }

    /**
     * Increments the counter by the specified amount.
     *
     * @param amount the amount to increment (must be non-negative)
     * @throws IllegalArgumentException if amount is negative
     */
    void increment(double amount);

    /**
     * Increments the counter with additional attributes.
     */
    void increment(double amount, Attributes attributes);

    /**
     * Returns the current value of the counter.
     */
    double value();

    @Override
    default MetricType type() {
        return MetricType.COUNTER;
    }
}
