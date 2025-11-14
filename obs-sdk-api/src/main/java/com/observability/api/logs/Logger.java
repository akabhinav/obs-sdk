package com.observability.api.logs;

import com.observability.api.common.Attributes;

/**
 * Logger interface for structured logging.
 * Provides fluent API for logging with attributes.
 */
public interface Logger {

    /**
     * Returns the logger name.
     */
    String getName();

    /**
     * Logs a message at the specified level.
     */
    void log(LogLevel level, String message);

    /**
     * Logs a message with attributes.
     */
    void log(LogLevel level, String message, Attributes attributes);

    /**
     * Logs a message with a throwable.
     */
    void log(LogLevel level, String message, Throwable throwable);

    /**
     * Logs a message with attributes and a throwable.
     */
    void log(LogLevel level, String message, Attributes attributes, Throwable throwable);

    /**
     * Logs a complete log record.
     */
    void log(LogRecord record);

    // Convenience methods

    default void trace(String message) {
        log(LogLevel.TRACE, message);
    }

    default void trace(String message, Attributes attributes) {
        log(LogLevel.TRACE, message, attributes);
    }

    default void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    default void debug(String message, Attributes attributes) {
        log(LogLevel.DEBUG, message, attributes);
    }

    default void info(String message) {
        log(LogLevel.INFO, message);
    }

    default void info(String message, Attributes attributes) {
        log(LogLevel.INFO, message, attributes);
    }

    default void warn(String message) {
        log(LogLevel.WARN, message);
    }

    default void warn(String message, Attributes attributes) {
        log(LogLevel.WARN, message, attributes);
    }

    default void warn(String message, Throwable throwable) {
        log(LogLevel.WARN, message, throwable);
    }

    default void error(String message) {
        log(LogLevel.ERROR, message);
    }

    default void error(String message, Attributes attributes) {
        log(LogLevel.ERROR, message, attributes);
    }

    default void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    default void error(String message, Attributes attributes, Throwable throwable) {
        log(LogLevel.ERROR, message, attributes, throwable);
    }

    default void fatal(String message) {
        log(LogLevel.FATAL, message);
    }

    default void fatal(String message, Throwable throwable) {
        log(LogLevel.FATAL, message, throwable);
    }

    /**
     * Checks if logging is enabled for the specified level.
     */
    boolean isEnabled(LogLevel level);
}
