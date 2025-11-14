package com.observability.api.logs;

import com.observability.api.common.Attributes;
import com.observability.api.trace.SpanContext;

import java.time.Instant;

/**
 * Immutable log record.
 * Uses Java 21 record for clean, concise data structure.
 */
public record LogRecord(
    Instant timestamp,
    LogLevel level,
    String message,
    String loggerName,
    Attributes attributes,
    SpanContext spanContext,
    Throwable throwable
) {

    /**
     * Builder for creating log records.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Instant timestamp = Instant.now();
        private LogLevel level = LogLevel.INFO;
        private String message = "";
        private String loggerName = "";
        private Attributes attributes = Attributes.empty();
        private SpanContext spanContext = SpanContext.invalid();
        private Throwable throwable = null;

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder level(LogLevel level) {
            this.level = level;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder loggerName(String loggerName) {
            this.loggerName = loggerName;
            return this;
        }

        public Builder attributes(Attributes attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder spanContext(SpanContext spanContext) {
            this.spanContext = spanContext;
            return this;
        }

        public Builder throwable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public LogRecord build() {
            return new LogRecord(
                timestamp,
                level,
                message,
                loggerName,
                attributes,
                spanContext,
                throwable
            );
        }
    }
}
