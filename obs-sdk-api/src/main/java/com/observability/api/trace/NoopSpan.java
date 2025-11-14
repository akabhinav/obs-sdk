package com.observability.api.trace;

import com.observability.api.common.Attributes;

import java.time.Instant;

/**
 * No-op implementation of Span for when tracing is disabled.
 */
final class NoopSpan implements Span {

    static final NoopSpan INSTANCE = new NoopSpan();

    private NoopSpan() {
    }

    @Override
    public SpanContext getSpanContext() {
        return SpanContext.invalid();
    }

    @Override
    public Span setAttribute(String key, Object value) {
        return this;
    }

    @Override
    public Span setAttributes(Attributes attributes) {
        return this;
    }

    @Override
    public Span addEvent(String name) {
        return this;
    }

    @Override
    public Span addEvent(String name, Attributes attributes) {
        return this;
    }

    @Override
    public Span addEvent(String name, Attributes attributes, Instant timestamp) {
        return this;
    }

    @Override
    public Span recordException(Throwable exception) {
        return this;
    }

    @Override
    public Span recordException(Throwable exception, Attributes attributes) {
        return this;
    }

    @Override
    public Span setStatus(SpanStatus status) {
        return this;
    }

    @Override
    public Span setStatus(SpanStatus status, String description) {
        return this;
    }

    @Override
    public Span updateName(String name) {
        return this;
    }

    @Override
    public void end() {
    }

    @Override
    public void end(Instant timestamp) {
    }

    @Override
    public boolean isRecording() {
        return false;
    }
}
