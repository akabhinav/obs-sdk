package com.observability.core.metrics;

import com.observability.api.common.Attributes;
import com.observability.api.metrics.Counter;

import java.util.concurrent.atomic.DoubleAdder;

/**
 * Thread-safe implementation of Counter using DoubleAdder for high performance.
 */
public final class CounterImpl implements Counter {

    private final String name;
    private final String description;
    private final String unit;
    private final Attributes attributes;
    private final DoubleAdder value;

    public CounterImpl(String name, String description, String unit, Attributes attributes) {
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.attributes = attributes;
        this.value = new DoubleAdder();
    }

    @Override
    public void increment(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Counter increment must be non-negative");
        }
        value.add(amount);
    }

    @Override
    public void increment(double amount, Attributes additionalAttributes) {
        // For simplicity, we ignore additional attributes in this implementation
        // A more sophisticated implementation could maintain separate counters per attribute set
        increment(amount);
    }

    @Override
    public double value() {
        return value.sum();
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
        return String.format("Counter{name='%s', value=%.2f}", name, value());
    }
}
