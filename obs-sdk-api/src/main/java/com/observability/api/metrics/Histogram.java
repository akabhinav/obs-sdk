package com.observability.api.metrics;

import com.observability.api.common.Attributes;

/**
 * Histogram metric - tracks distribution of values.
 * Useful for measuring request durations, response sizes, etc.
 */
public non-sealed interface Histogram extends Metric {

    /**
     * Records a value in the histogram.
     *
     * @param value the value to record
     */
    void record(double value);

    /**
     * Records a value with additional attributes.
     */
    void record(double value, Attributes attributes);

    /**
     * Returns statistics about recorded values.
     */
    HistogramSnapshot snapshot();

    @Override
    default MetricType type() {
        return MetricType.HISTOGRAM;
    }

    /**
     * Snapshot of histogram statistics.
     */
    record HistogramSnapshot(
        long count,
        double sum,
        double min,
        double max,
        double mean,
        double[] percentiles,
        double[] percentileValues
    ) {
        public HistogramSnapshot {
            if (count < 0) {
                throw new IllegalArgumentException("count must be non-negative");
            }
            if (percentiles.length != percentileValues.length) {
                throw new IllegalArgumentException("percentiles and values must have same length");
            }
        }
    }
}
