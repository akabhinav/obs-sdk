# Testing Guide - Observability SDK

Complete guide to testing all supported exporters locally and in cloud.

## 🚀 Quick Test (Console Exporter)

The fastest way to verify the SDK works:

```bash
# Use the default console configuration
cd obs-sdk-examples
mvn exec:java -Dexec.mainClass="com.observability.examples.QuickStartExample"
```

You'll see metrics, traces, and logs printed to console immediately!

## 📋 Available Test Examples

### 1. Quick Start Example
**File**: `QuickStartExample.java`
**What it tests**: Basic SDK functionality with console output
**Run**:
```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.QuickStartExample"
```

### 2. Comprehensive Example
**File**: `ComprehensiveExample.java`
**What it tests**: All SDK features (metrics, traces, logs, virtual threads)
**Run**:
```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.ComprehensiveExample"
```

### 3. Config-Based Example
**File**: `ConfigBasedExample.java`
**What it tests**: YAML configuration auto-loading
**Run**:
```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.ConfigBasedExample"
```

## 🐳 Local Testing with Docker

### Option 1: Full Observability Stack (Prometheus + Jaeger + Loki)

```bash
cd obs-sdk-examples
docker-compose up -d
```

This starts:
- **Prometheus** on http://localhost:9090 (metrics)
- **Jaeger** on http://localhost:16686 (traces)
- **Grafana** on http://localhost:3000 (visualization)
- **Prometheus Pushgateway** on http://localhost:9091 (push metrics)

Use config: `config-examples/prometheus-jaeger.yml`

```bash
export OBSERVABILITY_CONFIG=../config-examples/prometheus-jaeger.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.ConfigBasedExample"
```

**Verify**:
- Open http://localhost:9090/targets - See metrics
- Open http://localhost:16686 - See traces
- Open http://localhost:3000 - Visualize in Grafana

### Option 2: OpenTelemetry Collector

```bash
cd obs-sdk-examples
docker-compose -f docker-compose-otlp.yml up -d
```

This starts an OTLP collector that can forward to any backend.

Use config:
```yaml
exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "http://localhost:4318/v1/metrics"
```

## ☁️ Cloud Platform Testing

### Grafana Cloud (FREE Tier Available)

**Setup**:
1. Sign up at https://grafana.com/products/cloud/
2. Get your credentials from Grafana Cloud portal
3. Create config:

```yaml
# observability-grafana.yml
service:
  name: "test-service"
  version: "1.0.0"

exporters:
  metrics:
    - type: "grafana-cloud"
      enabled: true
      endpoint: "https://otlp-gateway-prod-us-central-0.grafana.net/otlp/v1/metrics"
      headers:
        Authorization: "Basic <base64 of instanceId:apiKey>"

  traces:
    - type: "grafana-cloud"
      enabled: true
      endpoint: "https://otlp-gateway-prod-us-central-0.grafana.net/otlp/v1/traces"
      headers:
        Authorization: "Basic <base64 of instanceId:apiKey>"
```

**Run**:
```bash
export OBSERVABILITY_CONFIG=observability-grafana.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.ConfigBasedExample"
```

**Verify**: Open Grafana Cloud dashboard to see metrics and traces

### Datadog (FREE Trial Available)

**Setup**:
1. Sign up at https://www.datadoghq.com/
2. Get your API key from Datadog portal
3. Create config:

```yaml
# observability-datadog.yml
service:
  name: "test-service"

exporters:
  metrics:
    - type: "datadog"
      enabled: true
      headers:
        DD-API-KEY: "your-api-key-here"

  traces:
    - type: "datadog"
      enabled: true
      headers:
        DD-API-KEY: "your-api-key-here"
```

**Run**:
```bash
export OBSERVABILITY_CONFIG=observability-datadog.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.ConfigBasedExample"
```

**Verify**: Open Datadog APM to see metrics and traces

### New Relic (FREE Tier Available)

**Setup**:
1. Sign up at https://newrelic.com/
2. Get your license key from New Relic portal
3. Create config:

```yaml
# observability-newrelic.yml
service:
  name: "test-service"

exporters:
  metrics:
    - type: "newrelic"
      enabled: true
      endpoint: "https://otlp.nr-data.net:4318/v1/metrics"
      headers:
        api-key: "your-license-key-here"
```

**Run**:
```bash
export OBSERVABILITY_CONFIG=observability-newrelic.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.ConfigBasedExample"
```

**Verify**: Open New Relic One to see metrics

## 🧪 Platform-Specific Tests

### Test 1: Console (No Setup Required)

```bash
# Uses default console exporter
mvn exec:java -Dexec.mainClass="com.observability.examples.QuickStartExample"
```

**Expected Output**:
```
=== Metrics Export ===
Counter: http.requests = 50.00 requests
Histogram: http.latency (ms)
  Count: 50
  Mean: 125.45
=====================
```

### Test 2: Prometheus

**Start Prometheus**:
```bash
docker run -d -p 9090:9090 prom/prometheus
docker run -d -p 9091:9091 prom/pushgateway
```

**Config**:
```yaml
exporters:
  metrics:
    - type: "prometheus"
      enabled: true
      endpoint: "http://localhost:9091/metrics/job/test-app"
```

**Run & Verify**:
```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.ConfigBasedExample"
# Open http://localhost:9091/metrics
# You should see your metrics in Prometheus format
```

### Test 3: Jaeger (Distributed Tracing)

**Start Jaeger**:
```bash
docker run -d -p 16686:16686 -p 14268:14268 jaegertracing/all-in-one:latest
```

**Config**:
```yaml
exporters:
  traces:
    - type: "jaeger"
      enabled: true
      endpoint: "http://localhost:14268/api/traces"
```

**Run & Verify**:
```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.ComprehensiveExample"
# Open http://localhost:16686
# Search for service "demo-service"
# You should see trace spans with parent-child relationships
```

### Test 4: Multi-Backend (Console + Prometheus + Jaeger)

**Config**:
```yaml
exporters:
  metrics:
    - type: "console"
      enabled: true
    - type: "prometheus"
      enabled: true
      endpoint: "http://localhost:9091/metrics/job/test-app"

  traces:
    - type: "console"
      enabled: true
    - type: "jaeger"
      enabled: true
      endpoint: "http://localhost:14268/api/traces"
```

**Run**:
```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.ConfigBasedExample"
```

**Verify**:
- Console: See output in terminal
- Prometheus: http://localhost:9091/metrics
- Jaeger: http://localhost:16686

Data goes to **all three** simultaneously!

## 📊 Test Scenarios

### Scenario 1: High-Throughput Load Test

```java
// Create load-test.yml
exporters:
  metrics:
    - type: "console"
      enabled: true
      batch_size: 1000
      batch_timeout_ms: 1000
```

```bash
# Run with many concurrent requests
mvn exec:java -Dexec.mainClass="com.observability.examples.ComprehensiveExample"
```

**Expected**: SDK handles high concurrency with virtual threads

### Scenario 2: Error Handling Test

```yaml
# Configure invalid endpoint to test retry logic
exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "http://invalid-host:4318/v1/metrics"
      timeout_ms: 1000
```

**Expected**: SDK logs errors gracefully and continues operation

### Scenario 3: Configuration Change Without Restart

1. Start with console exporter
2. Add Prometheus to config
3. Restart application
4. Verify both console and Prometheus receive data

**No code changes required!**

## 🔍 Debugging & Verification

### Enable Detailed Logging

```java
System.setProperty("observability.debug", "true");
var sdk = ObservabilitySDKImpl.fromConfig();
```

### Verify Exporter Loading

SDK prints on startup:
```
✓ Loaded metric exporter: console
✓ Loaded metric exporter: prometheus
✓ Loaded span exporter: jaeger
✅ Observability SDK initialized:
   Service: my-service v1.0.0
   Metric Exporters: 2
   Span Exporters: 1
   Log Exporters: 1
```

### Check HTTP Traffic

```bash
# Monitor HTTP requests to backends
tcpdump -i any -A 'tcp port 4318'  # OTLP
tcpdump -i any -A 'tcp port 9091'  # Prometheus
```

### Test Configuration Parsing

```bash
# Validate YAML syntax
yamllint observability.yml

# Test config loading without running app
java -cp target/classes com.observability.core.config.ConfigLoader
```

## 📝 Test Checklist

Before deploying to production, verify:

- [ ] Console exporter works
- [ ] OTLP exporter works (local collector)
- [ ] Prometheus exporter works (local pushgateway)
- [ ] Jaeger exporter works (local all-in-one)
- [ ] Configuration auto-loads from YAML
- [ ] Multiple exporters work simultaneously
- [ ] Metrics are accurate
- [ ] Traces show parent-child relationships
- [ ] Logs include trace context
- [ ] Virtual threads handle concurrency
- [ ] Errors are handled gracefully
- [ ] SDK shuts down cleanly

## 🎯 Platform Comparison Matrix

| Platform | Metrics | Traces | Logs | Free Tier | Setup Time |
|----------|---------|--------|------|-----------|------------|
| Console | ✅ | ✅ | ✅ | ✅ Free | 0 min |
| Prometheus | ✅ | ❌ | ❌ | ✅ Free | 2 min |
| Jaeger | ❌ | ✅ | ❌ | ✅ Free | 2 min |
| Grafana Cloud | ✅ | ✅ | ✅ | ✅ Free tier | 5 min |
| Datadog | ✅ | ✅ | ✅ | ⚠️ Trial | 5 min |
| New Relic | ✅ | ✅ | ✅ | ✅ Free tier | 5 min |
| OTLP Collector | ✅ | ✅ | ✅ | ✅ Free | 3 min |

## 🆘 Troubleshooting

### Exporter Not Loading

**Symptom**: `✗ Failed to load metric exporter: prometheus`

**Solutions**:
1. Check exporter module is on classpath
2. Verify configuration syntax
3. Check network connectivity to endpoint

### Metrics Not Appearing

**Symptom**: SDK runs but no metrics in backend

**Solutions**:
1. Check endpoint URL is correct
2. Verify authentication headers
3. Check backend is running and accessible
4. Enable debug logging
5. Test with console exporter first

### Connection Refused

**Symptom**: `Connection refused` errors

**Solutions**:
1. Verify backend is running: `docker ps`
2. Check port mappings
3. Test connectivity: `curl http://localhost:4318/v1/metrics`
4. Check firewall rules

### Configuration Not Found

**Symptom**: `No configuration file found, using defaults`

**Solutions**:
1. Verify file exists: `ls -la observability.yml`
2. Check file location (current directory, config/, classpath)
3. Use environment variable: `export OBSERVABILITY_CONFIG=path/to/config.yml`
4. Check YAML syntax

## 📚 Additional Resources

- **Example Configs**: `config-examples/` directory
- **API Documentation**: Run `mvn javadoc:javadoc`
- **Configuration Guide**: See `CONFIGURATION.md`
- **OpenTelemetry Docs**: https://opentelemetry.io/docs/
- **Prometheus Docs**: https://prometheus.io/docs/

## 🎓 Best Practices for Testing

1. **Start Simple**: Test with console exporter first
2. **Test Locally**: Use Docker for local testing
3. **One at a Time**: Test each exporter individually
4. **Multi-Backend**: Then test multiple exporters together
5. **Load Testing**: Test with high concurrency
6. **Error Scenarios**: Test with invalid configs
7. **Cloud Testing**: Finally test with cloud platforms

---

**Happy Testing!** 🚀 If you encounter issues, check the troubleshooting section or create an issue on GitHub.
