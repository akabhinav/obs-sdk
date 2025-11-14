package com.observability.core.trace;

import com.observability.api.common.Attributes;
import com.observability.api.trace.*;
import com.observability.core.context.ContextManager;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Implementation of Tracer with context propagation support.
 */
public final class TracerImpl implements Tracer {

    private final String instrumentationScopeName;
    private final String instrumentationScopeVersion;
    private final Random random = new SecureRandom();

    public TracerImpl(String instrumentationScopeName, String instrumentationScopeVersion) {
        this.instrumentationScopeName = instrumentationScopeName;
        this.instrumentationScopeVersion = instrumentationScopeVersion;
    }

    @Override
    public SpanBuilder spanBuilder(String spanName) {
        return new SpanBuilderImpl(spanName);
    }

    private String generateTraceId() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    private String generateSpanId() {
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private final class SpanBuilderImpl implements SpanBuilder {

        private final String spanName;
        private SpanContext parentContext;
        private SpanKind spanKind = SpanKind.INTERNAL;
        private Attributes.Builder attributesBuilder = Attributes.builder();
        private boolean noParent = false;

        private SpanBuilderImpl(String spanName) {
            this.spanName = spanName;
        }

        @Override
        public SpanBuilder setParent(SpanContext parentContext) {
            this.parentContext = parentContext;
            return this;
        }

        @Override
        public SpanBuilder setSpanKind(SpanKind spanKind) {
            this.spanKind = spanKind;
            return this;
        }

        @Override
        public SpanBuilder setAttribute(String key, Object value) {
            attributesBuilder.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder setAttributes(Attributes attributes) {
            attributesBuilder.putAll(attributes.asMap());
            return this;
        }

        @Override
        public SpanBuilder setNoParent() {
            this.noParent = true;
            this.parentContext = null;
            return this;
        }

        @Override
        public Span startSpan() {
            SpanContext effectiveParent = noParent ? null :
                (parentContext != null ? parentContext : ContextManager.getCurrentSpanContext());

            String traceId;
            if (effectiveParent != null && effectiveParent.isValid()) {
                traceId = effectiveParent.traceId();
            } else {
                traceId = generateTraceId();
            }

            String spanId = generateSpanId();
            SpanContext spanContext = SpanContext.create(traceId, spanId, (byte) 1, true);

            SpanImpl span = new SpanImpl(spanName, spanContext);
            span.setAttributes(attributesBuilder.build());

            return span;
        }
    }
}
