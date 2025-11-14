package com.observability.exporters.otlp;

import com.observability.api.exporter.Exporter;
import com.observability.api.exporter.SpanExporter;
import com.observability.api.trace.Span;
import com.observability.exporters.http.HttpExporter;

import java.net.http.HttpResponse;
import java.util.*;

/**
 * OpenTelemetry Protocol (OTLP) span exporter for distributed tracing.
 * Works with Jaeger, Zipkin, and all OTLP-compatible backends.
 */
public class OtlpSpanExporter extends HttpExporter implements SpanExporter {

    public OtlpSpanExporter(String endpoint, Map<String, String> headers, int timeoutMs) {
        super(endpoint, headers, timeoutMs);
    }

    @Override
    public ExportResult export(Collection<Span> spans) {
        if (shutdown) {
            return ExportResult.FAILURE_NOT_RETRYABLE;
        }

        try {
            Map<String, Object> otlpPayload = convertToOtlpFormat(spans);
            HttpResponse<String> response = sendJson(otlpPayload);

            if (isSuccessful(response)) {
                return ExportResult.SUCCESS;
            } else if (isRetryable(response)) {
                return ExportResult.FAILURE_RETRYABLE;
            } else {
                System.err.println("OTLP span export failed: " + response.statusCode());
                return ExportResult.FAILURE_NOT_RETRYABLE;
            }
        } catch (Exception e) {
            System.err.println("Error exporting spans to OTLP: " + e.getMessage());
            return ExportResult.FAILURE_RETRYABLE;
        }
    }

    private Map<String, Object> convertToOtlpFormat(Collection<Span> spans) {
        List<Map<String, Object>> resourceSpans = new ArrayList<>();
        List<Map<String, Object>> scopeSpans = new ArrayList<>();
        List<Map<String, Object>> spansList = new ArrayList<>();

        for (Span span : spans) {
            Map<String, Object> otlpSpan = new HashMap<>();
            otlpSpan.put("traceId", base64Encode(span.getSpanContext().traceId()));
            otlpSpan.put("spanId", base64Encode(span.getSpanContext().spanId()));
            otlpSpan.put("name", span.toString());
            otlpSpan.put("kind", 1); // INTERNAL
            otlpSpan.put("startTimeUnixNano", System.currentTimeMillis() * 1_000_000);
            otlpSpan.put("endTimeUnixNano", System.currentTimeMillis() * 1_000_000);

            spansList.add(otlpSpan);
        }

        Map<String, Object> scopeSpan = new HashMap<>();
        scopeSpan.put("spans", spansList);
        scopeSpans.add(scopeSpan);

        Map<String, Object> resourceSpan = new HashMap<>();
        resourceSpan.put("scopeSpans", scopeSpans);
        resourceSpans.add(resourceSpan);

        Map<String, Object> result = new HashMap<>();
        result.put("resourceSpans", resourceSpans);
        return result;
    }

    private String base64Encode(String hexString) {
        return Base64.getEncoder().encodeToString(hexString.getBytes());
    }

    @Override
    public ExportResult flush() {
        return ExportResult.SUCCESS;
    }

    @Override
    public void shutdown() {
        close();
    }
}
