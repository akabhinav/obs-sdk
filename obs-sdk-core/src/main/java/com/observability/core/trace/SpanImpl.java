package com.observability.core.trace;

import com.observability.api.common.Attributes;
import com.observability.api.trace.Span;
import com.observability.api.trace.SpanContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe implementation of Span.
 */
public final class SpanImpl implements Span {

    private final String name;
    private final SpanContext spanContext;
    private final Instant startTime;
    private final Lock lock = new ReentrantLock();

    private volatile boolean ended = false;
    private volatile String updatedName;
    private volatile SpanStatus status = SpanStatus.UNSET;
    private volatile String statusDescription;
    private volatile Instant endTime;

    private final List<Event> events = new ArrayList<>();
    private Attributes.Builder attributesBuilder = Attributes.builder();

    public SpanImpl(String name, SpanContext spanContext) {
        this.name = name;
        this.updatedName = name;
        this.spanContext = spanContext;
        this.startTime = Instant.now();
    }

    @Override
    public SpanContext getSpanContext() {
        return spanContext;
    }

    @Override
    public Span setAttribute(String key, Object value) {
        if (!ended) {
            lock.lock();
            try {
                if (!ended) {
                    attributesBuilder.put(key, value);
                }
            } finally {
                lock.unlock();
            }
        }
        return this;
    }

    @Override
    public Span setAttributes(Attributes attributes) {
        if (!ended) {
            lock.lock();
            try {
                if (!ended) {
                    attributesBuilder.putAll(attributes.asMap());
                }
            } finally {
                lock.unlock();
            }
        }
        return this;
    }

    @Override
    public Span addEvent(String name) {
        return addEvent(name, Attributes.empty(), Instant.now());
    }

    @Override
    public Span addEvent(String name, Attributes attributes) {
        return addEvent(name, attributes, Instant.now());
    }

    @Override
    public Span addEvent(String name, Attributes attributes, Instant timestamp) {
        if (!ended) {
            lock.lock();
            try {
                if (!ended) {
                    events.add(new Event(name, attributes, timestamp));
                }
            } finally {
                lock.unlock();
            }
        }
        return this;
    }

    @Override
    public Span recordException(Throwable exception) {
        return recordException(exception, Attributes.empty());
    }

    @Override
    public Span recordException(Throwable exception, Attributes attributes) {
        Objects.requireNonNull(exception, "exception cannot be null");

        Attributes exceptionAttributes = Attributes.builder()
            .put("exception.type", exception.getClass().getName())
            .put("exception.message", exception.getMessage())
            .putAll(attributes.asMap())
            .build();

        return addEvent("exception", exceptionAttributes);
    }

    @Override
    public Span setStatus(SpanStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public Span setStatus(SpanStatus status, String description) {
        this.status = status;
        this.statusDescription = description;
        return this;
    }

    @Override
    public Span updateName(String name) {
        this.updatedName = name;
        return this;
    }

    @Override
    public void end() {
        end(Instant.now());
    }

    @Override
    public void end(Instant timestamp) {
        if (!ended) {
            lock.lock();
            try {
                if (!ended) {
                    this.endTime = timestamp;
                    this.ended = true;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean isRecording() {
        return !ended;
    }

    public String getName() {
        return updatedName;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public SpanStatus getStatus() {
        return status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public Attributes getAttributes() {
        return attributesBuilder.build();
    }

    public List<Event> getEvents() {
        return List.copyOf(events);
    }

    @Override
    public String toString() {
        return String.format(
            "Span{name='%s', traceId='%s', spanId='%s', status=%s}",
            updatedName,
            spanContext.traceId(),
            spanContext.spanId(),
            status
        );
    }

    /**
     * Represents an event within a span.
     */
    public record Event(String name, Attributes attributes, Instant timestamp) {
    }
}
