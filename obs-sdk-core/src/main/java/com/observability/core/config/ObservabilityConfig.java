package com.observability.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main configuration for the Observability SDK.
 * Supports YAML/JSON configuration files.
 */
public class ObservabilityConfig {

    @JsonProperty("service")
    private ServiceConfig service = new ServiceConfig();

    @JsonProperty("exporters")
    private ExportersConfig exporters = new ExportersConfig();

    @JsonProperty("metrics")
    private MetricsConfig metrics = new MetricsConfig();

    @JsonProperty("traces")
    private TracesConfig traces = new TracesConfig();

    @JsonProperty("logs")
    private LogsConfig logs = new LogsConfig();

    public ServiceConfig getService() {
        return service;
    }

    public void setService(ServiceConfig service) {
        this.service = service;
    }

    public ExportersConfig getExporters() {
        return exporters;
    }

    public void setExporters(ExportersConfig exporters) {
        this.exporters = exporters;
    }

    public MetricsConfig getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricsConfig metrics) {
        this.metrics = metrics;
    }

    public TracesConfig getTraces() {
        return traces;
    }

    public void setTraces(TracesConfig traces) {
        this.traces = traces;
    }

    public LogsConfig getLogs() {
        return logs;
    }

    public void setLogs(LogsConfig logs) {
        this.logs = logs;
    }

    /**
     * Service identification configuration.
     */
    public static class ServiceConfig {
        @JsonProperty("name")
        private String name = "unknown-service";

        @JsonProperty("version")
        private String version = "unknown";

        @JsonProperty("environment")
        private String environment = "production";

        @JsonProperty("attributes")
        private Map<String, Object> attributes = new HashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }
    }

    /**
     * Exporters configuration.
     */
    public static class ExportersConfig {
        @JsonProperty("metrics")
        private List<ExporterConfig> metrics = new ArrayList<>();

        @JsonProperty("traces")
        private List<ExporterConfig> traces = new ArrayList<>();

        @JsonProperty("logs")
        private List<ExporterConfig> logs = new ArrayList<>();

        public List<ExporterConfig> getMetrics() {
            return metrics;
        }

        public void setMetrics(List<ExporterConfig> metrics) {
            this.metrics = metrics;
        }

        public List<ExporterConfig> getTraces() {
            return traces;
        }

        public void setTraces(List<ExporterConfig> traces) {
            this.traces = traces;
        }

        public List<ExporterConfig> getLogs() {
            return logs;
        }

        public void setLogs(List<ExporterConfig> logs) {
            this.logs = logs;
        }
    }

    /**
     * Individual exporter configuration.
     */
    public static class ExporterConfig {
        @JsonProperty("type")
        private String type;

        @JsonProperty("enabled")
        private boolean enabled = true;

        @JsonProperty("endpoint")
        private String endpoint;

        @JsonProperty("headers")
        private Map<String, String> headers = new HashMap<>();

        @JsonProperty("timeout_ms")
        private int timeoutMs = 10000;

        @JsonProperty("batch_size")
        private int batchSize = 100;

        @JsonProperty("batch_timeout_ms")
        private int batchTimeoutMs = 5000;

        @JsonProperty("config")
        private Map<String, Object> config = new HashMap<>();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public int getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getBatchTimeoutMs() {
            return batchTimeoutMs;
        }

        public void setBatchTimeoutMs(int batchTimeoutMs) {
            this.batchTimeoutMs = batchTimeoutMs;
        }

        public Map<String, Object> getConfig() {
            return config;
        }

        public void setConfig(Map<String, Object> config) {
            this.config = config;
        }
    }

    /**
     * Metrics configuration.
     */
    public static class MetricsConfig {
        @JsonProperty("enabled")
        private boolean enabled = true;

        @JsonProperty("export_interval_ms")
        private int exportIntervalMs = 60000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getExportIntervalMs() {
            return exportIntervalMs;
        }

        public void setExportIntervalMs(int exportIntervalMs) {
            this.exportIntervalMs = exportIntervalMs;
        }
    }

    /**
     * Traces configuration.
     */
    public static class TracesConfig {
        @JsonProperty("enabled")
        private boolean enabled = true;

        @JsonProperty("sampler")
        private String sampler = "always_on";

        @JsonProperty("sample_rate")
        private double sampleRate = 1.0;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getSampler() {
            return sampler;
        }

        public void setSampler(String sampler) {
            this.sampler = sampler;
        }

        public double getSampleRate() {
            return sampleRate;
        }

        public void setSampleRate(double sampleRate) {
            this.sampleRate = sampleRate;
        }
    }

    /**
     * Logs configuration.
     */
    public static class LogsConfig {
        @JsonProperty("enabled")
        private boolean enabled = true;

        @JsonProperty("level")
        private String level = "INFO";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }
    }
}
