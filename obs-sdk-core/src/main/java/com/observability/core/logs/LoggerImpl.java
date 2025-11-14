package com.observability.core.logs;

import com.observability.api.common.Attributes;
import com.observability.api.logs.LogLevel;
import com.observability.api.logs.LogRecord;
import com.observability.api.logs.Logger;
import com.observability.core.context.ContextManager;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe implementation of Logger.
 */
public final class LoggerImpl implements Logger {

    private final String name;
    private final LogLevel minLevel;
    private final List<LogRecord> records = new CopyOnWriteArrayList<>();

    public LoggerImpl(String name, LogLevel minLevel) {
        this.name = name;
        this.minLevel = minLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void log(LogLevel level, String message) {
        log(level, message, Attributes.empty(), null);
    }

    @Override
    public void log(LogLevel level, String message, Attributes attributes) {
        log(level, message, attributes, null);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        log(level, message, Attributes.empty(), throwable);
    }

    @Override
    public void log(LogLevel level, String message, Attributes attributes, Throwable throwable) {
        if (!isEnabled(level)) {
            return;
        }

        LogRecord record = LogRecord.builder()
            .timestamp(Instant.now())
            .level(level)
            .message(message)
            .loggerName(name)
            .attributes(attributes)
            .spanContext(ContextManager.getCurrentSpanContext())
            .throwable(throwable)
            .build();

        log(record);
    }

    @Override
    public void log(LogRecord record) {
        if (isEnabled(record.level())) {
            records.add(record);
            // In a real implementation, this would send to exporters
            printToConsole(record);
        }
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return level.isGreaterOrEqual(minLevel);
    }

    private void printToConsole(LogRecord record) {
        System.out.printf("[%s] %s - %s: %s%n",
            record.timestamp(),
            record.level(),
            record.loggerName(),
            record.message()
        );

        if (record.throwable() != null) {
            record.throwable().printStackTrace();
        }
    }

    public List<LogRecord> getRecords() {
        return List.copyOf(records);
    }
}
