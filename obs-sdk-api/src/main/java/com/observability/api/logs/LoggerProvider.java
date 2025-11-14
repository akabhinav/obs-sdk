package com.observability.api.logs;

/**
 * Provider for creating Logger instances.
 */
public interface LoggerProvider {

    /**
     * Gets or creates a logger with the specified name.
     *
     * @param name the logger name
     * @return the logger instance
     */
    Logger getLogger(String name);

    /**
     * Gets or creates a logger for the specified class.
     *
     * @param clazz the class
     * @return the logger instance
     */
    default Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
}
