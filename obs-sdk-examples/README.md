# Observability SDK Examples

Complete examples and testing guide for the Observability SDK.

## 🚀 Quick Start

### Run Example with Console Output

```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.QuickStartExample"
```

You'll see metrics, traces, and logs printed to console immediately!

## 📚 Available Examples

### 1. QuickStartExample
**Minimal example** showing basic SDK usage.

```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.QuickStartExample"
```

**What it demonstrates**:
- SDK initialization
- Creating metrics
- Basic tracing
- Logging

### 2. ComprehensiveExample
**Full-featured example** showing all SDK capabilities.

```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.ComprehensiveExample"
```

**What it demonstrates**:
- All metric types (Counter, Gauge, Histogram)
- Distributed tracing with parent-child spans
- Structured logging with attributes
- Context propagation
- Virtual threads for concurrency
- Error handling

### 3. ConfigBasedExample
**Zero-code configuration** example.

```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.ConfigBasedExample"
```

**What it demonstrates**:
- YAML-based configuration
- Auto-loading exporters from config
- No code changes to switch backends

### 4. AllExportersTest
**Comprehensive test** for all configured exporters.

```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"
```

**What it demonstrates**:
- Testing any configured backend
- Generating realistic test data
- Multi-backend export
- Verification instructions

## 🐳 Local Testing with Docker

### Start All Backends

```bash
docker-compose up -d
```

This starts:
- **Prometheus** (http://localhost:9090) - Metrics
- **Pushgateway** (http://localhost:9091) - Push metrics
- **Jaeger** (http://localhost:16686) - Traces
- **Grafana** (http://localhost:3000) - Visualization (admin/admin)
- **OTLP Collector** (http://localhost:4318) - Universal receiver
- **Zipkin** (http://localhost:9411) - Alternative tracing

### Run Test Suite

```bash
chmod +x test-all-exporters.sh
./test-all-exporters.sh
```

This automated script tests all exporters!

### Stop Backends

```bash
docker-compose down
```

## 🎯 Testing Specific Backends

### Test Console (No Setup)

```bash
# Uses src/main/resources/observability.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"
```

### Test Prometheus

```bash
# Start Prometheus
docker-compose up -d prometheus pushgateway

# Use Prometheus config
export OBSERVABILITY_CONFIG=../config-examples/prometheus-jaeger.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"

# Verify: http://localhost:9090/graph
# Query: test_requests_total
```

### Test Jaeger

```bash
# Start Jaeger
docker-compose up -d jaeger

# Use Jaeger config
export OBSERVABILITY_CONFIG=../config-examples/prometheus-jaeger.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.ComprehensiveExample"

# Verify: http://localhost:16686
# Search for service "demo-service"
```

### Test OTLP Collector

```bash
# Start OTLP Collector
docker-compose up -d otel-collector

# Create OTLP config
cat > test-otlp.yml <<EOF
service:
  name: "test-service"
exporters:
  metrics:
    - type: "otlp"
      endpoint: "http://localhost:4318/v1/metrics"
  traces:
    - type: "otlp"
      endpoint: "http://localhost:4318/v1/traces"
EOF

# Run test
export OBSERVABILITY_CONFIG=test-otlp.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"

# Check collector logs
docker logs observability-otel-collector
```

### Test Multi-Backend

```bash
# Start all backends
docker-compose up -d

# Use multi-backend config
export OBSERVABILITY_CONFIG=../config-examples/multi-backend.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"

# Verify in all platforms:
# - Console: stdout
# - Prometheus: http://localhost:9090
# - Jaeger: http://localhost:16686
# - Grafana: http://localhost:3000
```

## ☁️ Cloud Platform Testing

### Grafana Cloud

1. Sign up at https://grafana.com/products/cloud/
2. Get your credentials
3. Create config:

```yaml
# grafana-test.yml
service:
  name: "my-test-app"
exporters:
  metrics:
    - type: "grafana-cloud"
      endpoint: "https://otlp-gateway-prod-us-central-0.grafana.net/otlp/v1/metrics"
      headers:
        Authorization: "Basic <your-base64-credentials>"
```

4. Run test:
```bash
export OBSERVABILITY_CONFIG=grafana-test.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"
```

5. Verify in Grafana Cloud dashboard

### Datadog

1. Sign up at https://www.datadoghq.com/
2. Get API key
3. Use config from `config-examples/datadog.yml`
4. Run and verify in Datadog APM

### New Relic

1. Sign up at https://newrelic.com/
2. Get license key
3. Create config with your credentials
4. Run and verify in New Relic One

## 📊 Verification

### Prometheus

```bash
# Check if metrics are in pushgateway
curl http://localhost:9091/metrics | grep test_requests

# Query in Prometheus
# Open http://localhost:9090/graph
# Query: sum(rate(test_requests_total[1m]))
```

### Jaeger

```bash
# Check Jaeger UI
# Open http://localhost:16686
# Select service: "demo-service" or "test-app"
# Click "Find Traces"
```

### Grafana

```bash
# Open http://localhost:3000
# Login: admin/admin
# Go to Explore
# Select Prometheus datasource
# Query: test_requests_total
```

## 🔧 Configuration Files

All configuration examples are in `../config-examples/`:

- `grafana-cloud.yml` - Grafana Cloud
- `datadog.yml` - Datadog
- `prometheus-jaeger.yml` - Prometheus + Jaeger
- `aws-cloudwatch.yml` - AWS CloudWatch
- `multi-backend.yml` - Multiple backends

## 🆘 Troubleshooting

### Metrics Not Appearing

1. Check SDK output for exporter loading
2. Verify backend is running: `docker ps`
3. Check connectivity: `curl http://localhost:9091/metrics`
4. Enable debug mode in SDK
5. Check backend logs: `docker logs <container-name>`

### Connection Refused

1. Verify endpoint in config matches running service
2. Check Docker port mappings
3. Test with console exporter first

### Configuration Not Loading

1. Check file exists: `ls -la observability.yml`
2. Verify YAML syntax: `yamllint observability.yml`
3. Use absolute path or environment variable
4. Check SDK startup output

## 📚 Additional Resources

- **Testing Guide**: `../TESTING.md`
- **Configuration Guide**: `../CONFIGURATION.md`
- **Main README**: `../README.md`

---

**Happy Testing!** 🚀
