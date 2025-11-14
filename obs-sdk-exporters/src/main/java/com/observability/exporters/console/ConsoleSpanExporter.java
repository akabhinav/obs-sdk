package com.observability.exporters.console;

import com.observability.api.exporter.Exporter;
import com.observability.api.exporter.SpanExporter;
import com.observability.api.trace.Span;

import java.util.Collection;

/**
 * Console exporter for spans - prints spans to console.
 * Useful for development and debugging.
 */
public final class ConsoleSpanExporter implements SpanExporter {

    private volatile boolean shutdown = false;

    @Override
    public ExportResult export(Collection<Span> spans) {
        if (shutdown) {
            return ExportResult.FAILURE_NOT_RETRYABLE;
        }

        try {
            System.out.println("\n=== Spans Export ===");
            for (Span span : spans) {
                exportSpan(span);
            }
            System.out.println("===================\n");
            return ExportResult.SUCCESS;
        } catch (Exception e) {
            System.err.println("Error exporting spans: " + e.getMessage());
            return ExportResult.FAILURE_RETRYABLE;
        }
    }

    private void exportSpan(Span span) {
        System.out.printf("Span: %s%n", span);
        System.out.printf("  Context: %s%n", span.getSpanContext());
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
