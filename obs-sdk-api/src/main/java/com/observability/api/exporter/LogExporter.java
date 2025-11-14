package com.observability.api.exporter;

import com.observability.api.logs.LogRecord;

/**
 * Exporter for log records.
 */
public interface LogExporter extends Exporter<LogRecord> {
}
