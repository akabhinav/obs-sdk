package com.observability.api.trace;

import com.observability.api.common.Attributes;

/**
 * No-op implementation of Tracer.
 */
final class NoopTracer implements Tracer {

    static final NoopTracer INSTANCE = new NoopTracer();

    private NoopTracer() {
    }

    @Override
    public SpanBuilder spanBuilder(String spanName) {
        return NoopSpanBuilder.INSTANCE;
    }

    private static final class NoopSpanBuilder implements SpanBuilder {

        static final NoopSpanBuilder INSTANCE = new NoopSpanBuilder();

        @Override
        public SpanBuilder setParent(SpanContext parentContext) {
            return this;
        }

        @Override
        public SpanBuilder setSpanKind(SpanKind spanKind) {
            return this;
        }

        @Override
        public SpanBuilder setAttribute(String key, Object value) {
            return this;
        }

        @Override
        public SpanBuilder setAttributes(Attributes attributes) {
            return this;
        }

        @Override
        public SpanBuilder setNoParent() {
            return this;
        }

        @Override
        public Span startSpan() {
            return NoopSpan.INSTANCE;
        }
    }
}
