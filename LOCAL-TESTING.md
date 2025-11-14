# Local Testing Guide

Complete guide to testing the Observability SDK locally using Docker.

## 🚀 Quick Start (3 Commands)

```bash
# 1. Start all observability backends
cd obs-sdk-examples
docker-compose up -d

# 2. Run the SDK test (without Docker)
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"

# 3. View results
open http://localhost:9090      # Prometheus
open http://localhost:16686     # Jaeger
open http://localhost:3000      # Grafana (admin/admin)
```

That's it! You'll see metrics, traces, and logs in all platforms.

---

## 🐳 Option 1: Test SDK Locally, Backends in Docker (Recommended)

This is the **fastest** way to test and iterate.

### Step 1: Start Backend Services

```bash
cd obs-sdk-examples
docker-compose up -d
```

**What starts:**
- ✅ Prometheus - http://localhost:9090
- ✅ Pushgateway - http://localhost:9091
- ✅ Jaeger - http://localhost:16686
- ✅ Grafana - http://localhost:3000 (admin/admin)
- ✅ OTLP Collector - http://localhost:4318
- ✅ Zipkin - http://localhost:9411

### Step 2: Run SDK Application

```bash
# Quick test
mvn exec:java -Dexec.mainClass="com.observability.examples.QuickStartExample"

# Comprehensive test
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"

# With specific config
export OBSERVABILITY_CONFIG=../config-examples/prometheus-jaeger.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.ComprehensiveExample"
```

### Step 3: Verify Results

**Prometheus:**
```bash
open http://localhost:9090/graph
# Query: test_requests_total
# Query: rate(test_requests_total[1m])
```

**Jaeger:**
```bash
open http://localhost:16686
# Select service: "demo-service" or "test-app"
# Click "Find Traces"
# See parent-child span relationships
```

**Grafana:**
```bash
open http://localhost:3000
# Login: admin/admin
# Go to "Explore"
# Select "Prometheus" datasource
# Query: test_requests_total
```

### Step 4: Stop Backends

```bash
docker-compose down
```

---

## 🐳 Option 2: Everything in Docker

Run both SDK and backends entirely in Docker.

### Build SDK Docker Image

```bash
# From project root
docker build -t obs-sdk:latest .
```

### Start Everything

```bash
cd obs-sdk-examples
docker-compose -f docker-compose-full.yml up
```

This starts:
- All backend services
- SDK application (runs test automatically)

### Run Different Examples

```bash
# Run quick start example
docker-compose -f docker-compose-full.yml run obs-sdk-app \
  mvn exec:java -Dexec.mainClass=com.observability.examples.QuickStartExample

# Run comprehensive example
docker-compose -f docker-compose-full.yml run obs-sdk-app \
  mvn exec:java -Dexec.mainClass=com.observability.examples.ComprehensiveExample

# Run with custom config
docker-compose -f docker-compose-full.yml run \
  -e OBSERVABILITY_CONFIG=/app/config-examples/multi-backend.yml \
  obs-sdk-app
```

### Check Logs

```bash
# SDK app logs
docker-compose -f docker-compose-full.yml logs obs-sdk-app

# Backend logs
docker logs obs-prometheus
docker logs obs-jaeger
docker logs obs-otel-collector
```

---

## 🧪 Testing Individual Backends

### Test Console Only (No Docker)

```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.QuickStartExample"
```

Output appears immediately in terminal!

### Test Prometheus

```bash
# 1. Start Prometheus
docker-compose up -d prometheus pushgateway

# 2. Create config
cat > test-prometheus.yml <<EOF
service:
  name: "test-app"
exporters:
  metrics:
    - type: "prometheus"
      enabled: true
      endpoint: "http://localhost:9091/metrics/job/test-app"
EOF

# 3. Run test
export OBSERVABILITY_CONFIG=test-prometheus.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"

# 4. Verify
curl http://localhost:9091/metrics | grep test_requests
open http://localhost:9090/graph
```

### Test Jaeger

```bash
# 1. Start Jaeger
docker-compose up -d jaeger

# 2. Create config
cat > test-jaeger.yml <<EOF
service:
  name: "test-app"
exporters:
  traces:
    - type: "jaeger"
      enabled: true
      endpoint: "http://localhost:14268/api/traces"
EOF

# 3. Run test
export OBSERVABILITY_CONFIG=test-jaeger.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.ComprehensiveExample"

# 4. Verify
open http://localhost:16686
# Search for service "test-app"
```

### Test OTLP Collector

```bash
# 1. Start collector
docker-compose up -d otel-collector prometheus jaeger

# 2. Create config
cat > test-otlp.yml <<EOF
service:
  name: "test-app"
exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "http://localhost:4318/v1/metrics"
  traces:
    - type: "otlp"
      enabled: true
      endpoint: "http://localhost:4318/v1/traces"
  logs:
    - type: "otlp"
      enabled: true
      endpoint: "http://localhost:4318/v1/logs"
EOF

# 3. Run test
export OBSERVABILITY_CONFIG=test-otlp.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"

# 4. Check collector logs
docker logs obs-otel-collector

# 5. Verify in Prometheus (via collector)
open http://localhost:9090
# Query: test_requests_total

# 6. Verify in Jaeger (via collector)
open http://localhost:16686
```

### Test All Backends Simultaneously

```bash
# 1. Start all backends
docker-compose up -d

# 2. Use multi-backend config
export OBSERVABILITY_CONFIG=../config-examples/multi-backend.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"

# 3. Verify in all platforms
open http://localhost:9090      # Prometheus
open http://localhost:16686     # Jaeger
open http://localhost:3000      # Grafana
```

Data appears in **ALL** platforms simultaneously!

---

## 📊 Docker Images Used

All images are **official** and pulled from Docker Hub:

| Service | Image | Purpose |
|---------|-------|---------|
| Prometheus | `prom/prometheus:latest` | Metrics storage |
| Pushgateway | `prom/pushgateway:latest` | Push metrics |
| Jaeger | `jaegertracing/all-in-one:latest` | Distributed tracing |
| OTLP Collector | `otel/opentelemetry-collector:latest` | Universal receiver |
| Grafana | `grafana/grafana:latest` | Visualization |
| Zipkin | `openzipkin/zipkin:latest` | Alternative tracing |
| SDK App | Custom (built from Dockerfile) | Your application |

---

## 🔧 Docker Commands Cheat Sheet

### Start Services

```bash
# Start all backends
docker-compose up -d

# Start specific services
docker-compose up -d prometheus jaeger

# Start with logs
docker-compose up

# Start with custom compose file
docker-compose -f docker-compose-full.yml up -d
```

### Stop Services

```bash
# Stop all
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Stop specific services
docker-compose stop prometheus
```

### View Logs

```bash
# All services
docker-compose logs

# Specific service
docker-compose logs jaeger

# Follow logs
docker-compose logs -f prometheus

# Last 100 lines
docker-compose logs --tail=100 otel-collector
```

### Check Status

```bash
# List running containers
docker-compose ps

# Check resource usage
docker stats

# Inspect network
docker network inspect obs-sdk-examples_observability
```

### Restart Services

```bash
# Restart all
docker-compose restart

# Restart specific
docker-compose restart prometheus
```

---

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Check what's using port 9090
lsof -i :9090

# Kill process
kill -9 <PID>

# Or change port in docker-compose.yml
ports:
  - "19090:9090"  # Use port 19090 instead
```

### Container Won't Start

```bash
# Check logs
docker-compose logs <service-name>

# Remove and recreate
docker-compose down
docker-compose up -d
```

### No Data in Backends

```bash
# 1. Verify SDK is sending data
# Check SDK output for: "✓ Loaded metric exporter"

# 2. Check network connectivity
docker-compose exec obs-sdk-app ping prometheus

# 3. Verify endpoint in config
cat observability.yml

# 4. Check backend logs
docker logs obs-prometheus
docker logs obs-jaeger
docker logs obs-otel-collector
```

### Grafana Can't Connect to Prometheus

```bash
# 1. Verify Prometheus is running
curl http://localhost:9090/-/healthy

# 2. Check Grafana datasource config
docker exec obs-grafana cat /etc/grafana/provisioning/datasources/datasources.yml

# 3. Use Docker network name
# In Grafana: http://prometheus:9090 (not localhost)
```

---

## 📝 Configuration Files

### docker-compose.yml
Basic setup with all backends

### docker-compose-full.yml
Includes SDK application container

### Dockerfile
Builds SDK application image

### prometheus.yml
Prometheus scrape configuration

### otel-collector-config.yml
OTLP collector pipelines

### grafana-datasources.yml
Auto-configured Grafana datasources

---

## ✅ Verification Checklist

Before considering testing complete:

- [ ] Prometheus shows metrics: http://localhost:9090/graph
- [ ] Pushgateway has data: http://localhost:9091/metrics
- [ ] Jaeger shows traces: http://localhost:16686
- [ ] Grafana connects to datasources: http://localhost:3000
- [ ] OTLP collector receives data (check logs)
- [ ] SDK app runs without errors
- [ ] Metrics appear in Prometheus queries
- [ ] Traces show parent-child relationships in Jaeger
- [ ] Grafana can query Prometheus data

---

## 🎯 Common Test Scenarios

### Scenario 1: Quick Smoke Test

```bash
docker-compose up -d prometheus pushgateway
export OBSERVABILITY_CONFIG=../config-examples/prometheus-jaeger.yml
mvn exec:java -Dexec.mainClass="com.observability.examples.QuickStartExample"
curl http://localhost:9091/metrics | grep http_requests
```

### Scenario 2: Full Integration Test

```bash
docker-compose up -d
mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest"
# Check all dashboards
```

### Scenario 3: Performance Test

```bash
# Run multiple instances
for i in {1..10}; do
  mvn exec:java -Dexec.mainClass="com.observability.examples.AllExportersTest" &
done
wait
# Check Prometheus for load metrics
```

---

## 🌐 Access URLs

Once Docker Compose is running:

| Service | URL | Credentials |
|---------|-----|-------------|
| Prometheus | http://localhost:9090 | - |
| Pushgateway | http://localhost:9091 | - |
| Jaeger UI | http://localhost:16686 | - |
| Grafana | http://localhost:3000 | admin/admin |
| Zipkin | http://localhost:9411 | - |
| OTLP HTTP | http://localhost:4318 | - |
| OTLP gRPC | http://localhost:4317 | - |

---

## 💡 Tips

1. **Start Simple**: Test with console first, then add Docker backends
2. **One at a Time**: Test each backend individually before combining
3. **Check Logs**: Always check `docker-compose logs` if something doesn't work
4. **Use Networks**: Containers can reach each other by service name (e.g., `prometheus:9090`)
5. **Clean State**: Use `docker-compose down -v` to remove all data and start fresh

---

**Happy Local Testing!** 🚀

Need help? Check TESTING.md for more details or troubleshooting guide.
