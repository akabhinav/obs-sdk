package com.observability.exporters.otlp;

import com.observability.api.exporter.Exporter;
import com.observability.api.exporter.LogExporter;
import com.observability.api.logs.LogRecord;
import com.observability.exporters.http.HttpExporter;

import java.net.http.HttpResponse;
import java.util.*;

/**
 * OpenTelemetry Protocol (OTLP) log exporter.
 */
public class OtlpLogExporter extends HttpExporter implements LogExporter {

    public OtlpLogExporter(String endpoint, Map<String, String> headers, int timeoutMs) {
        super(endpoint, headers, timeoutMs);
    }

    @Override
    public ExportResult export(Collection<LogRecord> records) {
        if (shutdown) {
            return ExportResult.FAILURE_NOT_RETRYABLE;
        }

        try {
            Map<String, Object> otlpPayload = convertToOtlpFormat(records);
            HttpResponse<String> response = sendJson(otlpPayload);

            if (isSuccessful(response)) {
                return ExportResult.SUCCESS;
            } else if (isRetryable(response)) {
                return ExportResult.FAILURE_RETRYABLE;
            } else {
                return ExportResult.FAILURE_NOT_RETRYABLE;
            }
        } catch (Exception e) {
            System.err.println("Error exporting logs to OTLP: " + e.getMessage());
            return ExportResult.FAILURE_RETRYABLE;
        }
    }

    private Map<String, Object> convertToOtlpFormat(Collection<LogRecord> records) {
        List<Map<String, Object>> resourceLogs = new ArrayList<>();
        List<Map<String, Object>> scopeLogs = new ArrayList<>();
        List<Map<String, Object>> logRecords = new ArrayList<>();

        for (LogRecord record : records) {
            Map<String, Object> otlpLog = new HashMap<>();
            otlpLog.put("timeUnixNano", record.timestamp().toEpochMilli() * 1_000_000);
            otlpLog.put("severityText", record.level().toString());
            otlpLog.put("severityNumber", record.level().getSeverity());
            otlpLog.put("body", Map.of("stringValue", record.message()));

            logRecords.add(otlpLog);
        }

        Map<String, Object> scopeLog = new HashMap<>();
        scopeLog.put("logRecords", logRecords);
        scopeLogs.add(scopeLog);

        Map<String, Object> resourceLog = new HashMap<>();
        resourceLog.put("scopeLogs", scopeLogs);
        resourceLogs.add(resourceLog);

        Map<String, Object> result = new HashMap<>();
        result.put("resourceLogs", resourceLogs);
        return result;
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
