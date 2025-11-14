package com.observability.core.metrics;

import com.observability.api.common.Attributes;
import com.observability.api.metrics.Histogram;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe implementation of Histogram.
 * Uses a lock-based approach for accurate percentile calculations.
 */
public final class HistogramImpl implements Histogram {

    private final String name;
    private final String description;
    private final String unit;
    private final Attributes attributes;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    private static final int INITIAL_CAPACITY = 1000;
    private double[] values = new double[INITIAL_CAPACITY];
    private int count = 0;
    private double sum = 0;
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;

    public HistogramImpl(String name, String description, String unit, Attributes attributes) {
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.attributes = attributes;
    }

    @Override
    public void record(double value) {
        writeLock.lock();
        try {
            if (count == values.length) {
                values = Arrays.copyOf(values, values.length * 2);
            }
            values[count++] = value;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void record(double value, Attributes additionalAttributes) {
        record(value);
    }

    @Override
    public HistogramSnapshot snapshot() {
        readLock.lock();
        try {
            if (count == 0) {
                return new HistogramSnapshot(
                    0, 0, 0, 0, 0,
                    new double[]{50, 95, 99},
                    new double[]{0, 0, 0}
                );
            }

            double[] sortedValues = Arrays.copyOf(values, count);
            Arrays.sort(sortedValues);

            double mean = sum / count;
            double[] percentiles = {50, 95, 99};
            double[] percentileValues = new double[percentiles.length];

            for (int i = 0; i < percentiles.length; i++) {
                int index = (int) Math.ceil(percentiles[i] / 100.0 * count) - 1;
                percentileValues[i] = sortedValues[Math.max(0, Math.min(index, count - 1))];
            }

            return new HistogramSnapshot(
                count,
                sum,
                min,
                max,
                mean,
                percentiles,
                percentileValues
            );
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String unit() {
        return unit;
    }

    @Override
    public Attributes attributes() {
        return attributes;
    }

    @Override
    public String toString() {
        HistogramSnapshot snap = snapshot();
        return String.format(
            "Histogram{name='%s', count=%d, mean=%.2f, p50=%.2f, p95=%.2f, p99=%.2f}",
            name, snap.count(), snap.mean(),
            snap.percentileValues()[0],
            snap.percentileValues()[1],
            snap.percentileValues()[2]
        );
    }
}
