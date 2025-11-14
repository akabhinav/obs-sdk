package com.observability.api.exporter;

import com.observability.api.trace.Span;

/**
 * Exporter for span data.
 */
public interface SpanExporter extends Exporter<Span> {
}
