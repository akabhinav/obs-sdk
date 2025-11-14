package com.observability.api.logs;

/**
 * Enum representing log severity levels.
 */
public enum LogLevel {
    TRACE(1),
    DEBUG(5),
    INFO(9),
    WARN(13),
    ERROR(17),
    FATAL(21);

    private final int severity;

    LogLevel(int severity) {
        this.severity = severity;
    }

    public int getSeverity() {
        return severity;
    }

    public boolean isGreaterOrEqual(LogLevel other) {
        return this.severity >= other.severity;
    }
}
