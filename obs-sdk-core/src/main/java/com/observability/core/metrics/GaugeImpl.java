package com.observability.core.metrics;

import com.observability.api.common.Attributes;
import com.observability.api.metrics.Gauge;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Thread-safe implementation of Gauge.
 * Supports both direct value setting and callback-based values.
 */
public final class GaugeImpl implements Gauge {

    private final String name;
    private final String description;
    private final String unit;
    private final Attributes attributes;
    private final AtomicReference<Double> value;
    private final Supplier<Double> valueSupplier;

    public GaugeImpl(String name, String description, String unit, Attributes attributes) {
        this(name, description, unit, attributes, null);
    }

    public GaugeImpl(
        String name,
        String description,
        String unit,
        Attributes attributes,
        Supplier<Double> valueSupplier
    ) {
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.attributes = attributes;
        this.value = new AtomicReference<>(0.0);
        this.valueSupplier = valueSupplier;
    }

    @Override
    public void set(double value) {
        this.value.set(value);
    }

    @Override
    public void set(double value, Attributes additionalAttributes) {
        set(value);
    }

    @Override
    public void increment(double amount) {
        value.updateAndGet(current -> current + amount);
    }

    @Override
    public void decrement(double amount) {
        value.updateAndGet(current -> current - amount);
    }

    @Override
    public double value() {
        if (valueSupplier != null) {
            return valueSupplier.get();
        }
        return value.get();
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
        return String.format("Gauge{name='%s', value=%.2f}", name, value());
    }
}
