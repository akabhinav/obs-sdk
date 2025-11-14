package com.observability.core.metrics;

import com.observability.api.common.Attributes;
import com.observability.api.metrics.*;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Thread-safe implementation of MetricRegistry using ConcurrentHashMap.
 */
public final class MetricRegistryImpl implements MetricRegistry {

    private final ConcurrentMap<String, Metric> metrics = new ConcurrentHashMap<>();

    @Override
    public Counter counter(String name, String description, String unit) {
        return counter(name, description, unit, Attributes.empty());
    }

    @Override
    public Counter counter(String name, String description, String unit, Attributes attributes) {
        return (Counter) metrics.computeIfAbsent(name, k ->
            new CounterImpl(name, description, unit, attributes)
        );
    }

    @Override
    public Gauge gauge(String name, String description, String unit) {
        return gauge(name, description, unit, Attributes.empty());
    }

    @Override
    public Gauge gauge(String name, String description, String unit, Attributes attributes) {
        return (Gauge) metrics.computeIfAbsent(name, k ->
            new GaugeImpl(name, description, unit, attributes)
        );
    }

    @Override
    public Gauge gauge(String name, String description, String unit, Supplier<Double> valueSupplier) {
        return (Gauge) metrics.computeIfAbsent(name, k ->
            new GaugeImpl(name, description, unit, Attributes.empty(), valueSupplier)
        );
    }

    @Override
    public Histogram histogram(String name, String description, String unit) {
        return histogram(name, description, unit, Attributes.empty());
    }

    @Override
    public Histogram histogram(String name, String description, String unit, Attributes attributes) {
        return (Histogram) metrics.computeIfAbsent(name, k ->
            new HistogramImpl(name, description, unit, attributes)
        );
    }

    @Override
    public Collection<Metric> getMetrics() {
        return metrics.values();
    }

    @Override
    public boolean remove(String name) {
        return metrics.remove(name) != null;
    }

    @Override
    public void clear() {
        metrics.clear();
    }
}
