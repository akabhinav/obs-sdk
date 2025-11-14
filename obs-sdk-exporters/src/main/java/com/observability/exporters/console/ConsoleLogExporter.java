package com.observability.exporters.console;

import com.observability.api.exporter.Exporter;
import com.observability.api.exporter.LogExporter;
import com.observability.api.logs.LogRecord;

import java.util.Collection;

/**
 * Console exporter for logs - prints logs to console.
 * Useful for development and debugging.
 */
public final class ConsoleLogExporter implements LogExporter {

    private volatile boolean shutdown = false;

    @Override
    public ExportResult export(Collection<LogRecord> records) {
        if (shutdown) {
            return ExportResult.FAILURE_NOT_RETRYABLE;
        }

        try {
            System.out.println("\n=== Logs Export ===");
            for (LogRecord record : records) {
                exportLog(record);
            }
            System.out.println("==================\n");
            return ExportResult.SUCCESS;
        } catch (Exception e) {
            System.err.println("Error exporting logs: " + e.getMessage());
            return ExportResult.FAILURE_RETRYABLE;
        }
    }

    private void exportLog(LogRecord record) {
        System.out.printf("[%s] %s %s: %s%n",
            record.timestamp(),
            record.level(),
            record.loggerName(),
            record.message()
        );

        if (!record.attributes().isEmpty()) {
            System.out.printf("  Attributes: %s%n", record.attributes());
        }

        if (record.spanContext().isValid()) {
            System.out.printf("  TraceId: %s, SpanId: %s%n",
                record.spanContext().traceId(),
                record.spanContext().spanId()
            );
        }

        if (record.throwable() != null) {
            System.out.print("  Exception: ");
            record.throwable().printStackTrace();
        }
    }

    @Override
    public ExportResult flush() {
        return ExportResult.SUCCESS;
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }
}
