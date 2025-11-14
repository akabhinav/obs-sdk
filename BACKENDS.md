# Testing All 40+ Observability Backends

Guide to testing the Observability SDK with all supported platforms.

## 🔑 Key Concept: OTLP = Universal Protocol

The SDK supports **40+ backends** through **OpenTelemetry Protocol (OTLP)**:
- **1 exporter type** (`otlp`)
- **Works with 40+ platforms** (just change the endpoint!)

## 📊 All Supported Backends

### Tier 1: Native Implementations (Built-in)

| Backend | Type | Config | Local Test |
|---------|------|--------|------------|
| **Console** | `console` | No config needed | ✅ Instant |
| **Prometheus** | `prometheus` | Pushgateway | ✅ Docker |
| **Jaeger** | `jaeger` | OTLP or HTTP | ✅ Docker |
| **Zipkin** | `zipkin` | OTLP or HTTP | ✅ Docker |
| **OTLP Collector** | `otlp` | Any endpoint | ✅ Docker |

### Tier 2: OTLP-Native Platforms (Cloud)

These platforms accept OTLP **directly** - just change the endpoint:

| Platform | Type | Signals | Free Tier |
|----------|------|---------|-----------|
| **Grafana Cloud** | `otlp` | All | ✅ Yes |
| **Datadog** | `otlp` | All | Trial |
| **New Relic** | `otlp` | All | ✅ Yes |
| **Honeycomb** | `otlp` | All | ✅ Yes |
| **Lightstep** | `otlp` | All | Trial |
| **Elastic APM** | `otlp` | All | ✅ Yes |
| **Dynatrace** | `otlp` | All | Trial |
| **Azure Monitor** | `otlp` | All | Azure account |
| **Google Cloud** | `otlp` | All | GCP account |
| **AWS X-Ray** | `otlp` | Traces | AWS account |
| **Splunk** | `otlp` | All | Trial |
| **Sumo Logic** | `otlp` | All | ✅ Yes |
| **Logz.io** | `otlp` | All | ✅ Yes |
| **Coralogix** | `otlp` | All | Trial |
| **Chronosphere** | `otlp` | Metrics | Trial |

### Tier 3: OTLP via Collector (30+ More)

Use OpenTelemetry Collector to forward to:

- AppDynamics
- SignalFx
- Instana
- New Relic Insights
- InfluxDB
- Kafka
- And 20+ more...

---

## 🚀 How to Test Each Backend

### 1. Console (Instant - No Setup)

```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.QuickStartExample"
```

**Verification**: See output in terminal immediately

---

### 2. Prometheus (Docker - 2 minutes)

```bash
# Start Prometheus
cd obs-sdk-examples
docker-compose up -d prometheus pushgateway

# Create config
cat > test-prometheus.yml <<EOF
service:
  name: "test-app"
exporters:
  metrics:
    - type: "prometheus"
      enabled: true
      endpoint: "http://localhost:9091/metrics/job/test-app"
EOF

# Run test
export OBSERVABILITY_CONFIG=test-prometheus.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"

# Verify
open http://localhost:9090/graph
# Query: test_requests_total
```

---

### 3. Jaeger (Docker - 2 minutes)

```bash
# Start Jaeger
docker-compose up -d jaeger

# Create config
cat > test-jaeger.yml <<EOF
service:
  name: "test-app"
exporters:
  traces:
    - type: "jaeger"
      enabled: true
      endpoint: "http://localhost:14268/api/traces"
EOF

# Run test
export OBSERVABILITY_CONFIG=test-jaeger.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.ComprehensiveExample"

# Verify
open http://localhost:16686
# Search service: "test-app"
```

---

### 4. Grafana Cloud (5 minutes)

**Setup**:
1. Sign up: https://grafana.com/products/cloud/ (FREE tier)
2. Get credentials from portal (Stack → Details → OpenTelemetry)
3. Base64 encode: `echo -n "instanceID:apiKey" | base64`

**Config**:
```yaml
# test-grafana-cloud.yml
service:
  name: "test-app"

exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "https://otlp-gateway-prod-us-central-0.grafana.net/otlp/v1/metrics"
      headers:
        Authorization: "Basic <your-base64-credentials>"

  traces:
    - type: "otlp"
      enabled: true
      endpoint: "https://otlp-gateway-prod-us-central-0.grafana.net/otlp/v1/traces"
      headers:
        Authorization: "Basic <your-base64-credentials>"

  logs:
    - type: "otlp"
      enabled: true
      endpoint: "https://otlp-gateway-prod-us-central-0.grafana.net/otlp/v1/logs"
      headers:
        Authorization: "Basic <your-base64-credentials>"
```

**Run**:
```bash
export OBSERVABILITY_CONFIG=test-grafana-cloud.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"
```

**Verify**: Open Grafana Cloud → Explore → See your metrics, traces, logs

---

### 5. Datadog (5 minutes)

**Setup**:
1. Sign up: https://www.datadoghq.com/ (FREE trial)
2. Get API key from Organization Settings

**Config**:
```yaml
# test-datadog.yml
service:
  name: "test-app"

exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "https://api.datadoghq.com/api/v2/otlp/v1/metrics"
      headers:
        DD-API-KEY: "your-api-key-here"

  traces:
    - type: "otlp"
      enabled: true
      endpoint: "https://trace.agent.datadoghq.com/api/v0.2/traces"
      headers:
        DD-API-KEY: "your-api-key-here"
```

**Run**:
```bash
export OBSERVABILITY_CONFIG=test-datadog.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"
```

**Verify**: Open Datadog → APM → Services

---

### 6. New Relic (5 minutes)

**Setup**:
1. Sign up: https://newrelic.com/ (FREE tier)
2. Get license key from API Keys section

**Config**:
```yaml
# test-newrelic.yml
service:
  name: "test-app"

exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "https://otlp.nr-data.net:4318/v1/metrics"
      headers:
        api-key: "your-license-key-here"

  traces:
    - type: "otlp"
      enabled: true
      endpoint: "https://otlp.nr-data.net:4318/v1/traces"
      headers:
        api-key: "your-license-key-here"
```

**Run**:
```bash
export OBSERVABILITY_CONFIG=test-newrelic.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"
```

**Verify**: Open New Relic One → Services

---

### 7. Honeycomb (5 minutes)

**Setup**:
1. Sign up: https://www.honeycomb.io/ (FREE tier)
2. Get API key from Team Settings

**Config**:
```yaml
# test-honeycomb.yml
service:
  name: "test-app"

exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "https://api.honeycomb.io/v1/metrics"
      headers:
        x-honeycomb-team: "your-api-key-here"
        x-honeycomb-dataset: "test-dataset"

  traces:
    - type: "otlp"
      enabled: true
      endpoint: "https://api.honeycomb.io/v1/traces"
      headers:
        x-honeycomb-team: "your-api-key-here"
```

**Run & Verify**: Check Honeycomb dashboard

---

### 8. Elastic APM (Docker or Cloud)

**Local (Docker)**:
```bash
# Start Elastic stack
docker run -d -p 9200:9200 -p 5601:5601 \
  -e "discovery.type=single-node" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0

docker run -d -p 8200:8200 \
  docker.elastic.co/apm/apm-server:8.11.0
```

**Config**:
```yaml
# test-elastic.yml
service:
  name: "test-app"

exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "http://localhost:8200/v1/metrics"

  traces:
    - type: "otlp"
      enabled: true
      endpoint: "http://localhost:8200/v1/traces"
```

**Verify**: Open Kibana → APM

---

### 9. Azure Monitor (Cloud)

**Setup**: Requires Azure account and Application Insights resource

**Config**:
```yaml
# test-azure.yml
service:
  name: "test-app"

exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "https://dc.services.visualstudio.com/v2/track"
      headers:
        Authorization: "InstrumentationKey=your-key"

  traces:
    - type: "otlp"
      enabled: true
      endpoint: "https://dc.services.visualstudio.com/v2/track"
      headers:
        Authorization: "InstrumentationKey=your-key"
```

---

### 10. Google Cloud Operations (Cloud)

**Setup**: Requires GCP account

**Config**:
```yaml
# test-gcp.yml
service:
  name: "test-app"

exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "https://cloudtrace.googleapis.com/v2/projects/YOUR_PROJECT_ID/traces"

  traces:
    - type: "otlp"
      enabled: true
      endpoint: "https://cloudtrace.googleapis.com/v2/projects/YOUR_PROJECT_ID/traces"
```

---

### 11. Lightstep (Cloud)

**Setup**: Sign up at https://lightstep.com/

**Config**:
```yaml
# test-lightstep.yml
service:
  name: "test-app"

exporters:
  traces:
    - type: "otlp"
      enabled: true
      endpoint: "https://ingest.lightstep.com:443/traces/otlp/v1"
      headers:
        lightstep-access-token: "your-token-here"
```

---

### 12. Dynatrace (Cloud)

**Setup**: Sign up at https://www.dynatrace.com/

**Config**:
```yaml
# test-dynatrace.yml
service:
  name: "test-app"

exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "https://{your-environment-id}.live.dynatrace.com/api/v2/otlp/v1/metrics"
      headers:
        Authorization: "Api-Token your-token"
```

---

### 13. Splunk (Cloud or Enterprise)

**Config**:
```yaml
# test-splunk.yml
service:
  name: "test-app"

exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "https://ingest.{realm}.signalfx.com/v2/datapoint/otlp"
      headers:
        X-SF-Token: "your-token"
```

---

### 14. Sumo Logic (Cloud)

**Config**:
```yaml
# test-sumologic.yml
service:
  name: "test-app"

exporters:
  traces:
    - type: "otlp"
      enabled: true
      endpoint: "https://otlp.{deployment}.sumologic.com/v1/traces"
      headers:
        X-Sumo-Metadata: "your-metadata"
```

---

### 15-40+: Via OTLP Collector

For any other backend, use the OpenTelemetry Collector as a bridge:

```bash
# Start collector
docker-compose up -d otel-collector

# Configure collector to forward to your backend
# Edit: otel-collector-config.yml

# Use OTLP to send to collector
cat > test-any-backend.yml <<EOF
exporters:
  metrics:
    - type: "otlp"
      endpoint: "http://localhost:4318/v1/metrics"
EOF
```

**Collector can forward to**:
- InfluxDB
- Kafka
- SignalFx
- AppDynamics
- Instana
- CloudWatch
- And 20+ more backends

---

## 📋 Quick Testing Matrix

| Backend | Setup Time | Free Tier | Local Docker | Cloud Only |
|---------|------------|-----------|--------------|------------|
| Console | 0 min | ✅ | ✅ | - |
| Prometheus | 2 min | ✅ | ✅ | - |
| Jaeger | 2 min | ✅ | ✅ | - |
| OTLP Collector | 2 min | ✅ | ✅ | - |
| Grafana Cloud | 5 min | ✅ | - | ✅ |
| Honeycomb | 5 min | ✅ | - | ✅ |
| New Relic | 5 min | ✅ | - | ✅ |
| Elastic APM | 5 min | ✅ | ✅ | ✅ |
| Datadog | 5 min | Trial | - | ✅ |
| Lightstep | 5 min | Trial | - | ✅ |
| Dynatrace | 5 min | Trial | - | ✅ |
| Azure | 10 min | Azure $$  | - | ✅ |
| GCP | 10 min | GCP $$ | - | ✅ |

---

## 🎯 Automated Testing Script

I'll create a script to test multiple backends:

```bash
#!/bin/bash
# test-multiple-backends.sh

BACKENDS=("console" "prometheus" "jaeger" "grafana-cloud" "datadog" "newrelic")

for backend in "${BACKENDS[@]}"; do
    echo "Testing $backend..."

    export OBSERVABILITY_CONFIG="configs/test-$backend.yml"

    if mvn -q exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"; then
        echo "✅ $backend - PASSED"
    else
        echo "❌ $backend - FAILED"
    fi

    sleep 5
done
```

---

## 💡 Pro Tips

1. **Start Local**: Test console, Prometheus, Jaeger first
2. **Use Free Tiers**: Grafana Cloud, Honeycomb, New Relic have generous free tiers
3. **OTLP is Universal**: One config works everywhere, just change endpoint
4. **Use Collector**: For complex scenarios, use OTLP Collector as intermediary
5. **Test Multi-Backend**: Send to multiple platforms simultaneously

---

## 📚 Next Steps

Want to test a specific backend? Let me create a detailed config for:
- AWS X-Ray
- GCP Cloud Trace
- Azure Application Insights
- Any other platform!

All you need is the **OTLP endpoint** and **authentication**!

---

**The beauty of OTLP: Write once, run anywhere!** 🚀
