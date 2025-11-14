package com.observability.core.logs;

import com.observability.api.logs.LogLevel;
import com.observability.api.logs.Logger;
import com.observability.api.logs.LoggerProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe implementation of LoggerProvider.
 */
public final class LoggerProviderImpl implements LoggerProvider {

    private final ConcurrentMap<String, Logger> loggers = new ConcurrentHashMap<>();
    private final LogLevel minLevel;

    public LoggerProviderImpl() {
        this(LogLevel.INFO);
    }

    public LoggerProviderImpl(LogLevel minLevel) {
        this.minLevel = minLevel;
    }

    @Override
    public Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, k -> new LoggerImpl(name, minLevel));
    }
}
