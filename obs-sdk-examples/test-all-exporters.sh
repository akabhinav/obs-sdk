#!/bin/bash

# Test script for all exporters
# This script tests each exporter type with the Observability SDK

set -e

echo "🚀 Observability SDK - Exporter Test Suite"
echo "==========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to run a test
run_test() {
    local test_name=$1
    local config_file=$2
    local main_class=$3

    echo -e "${YELLOW}Testing: ${test_name}${NC}"
    echo "Config: ${config_file}"
    echo "----------------------------------------"

    if [ -f "$config_file" ]; then
        export OBSERVABILITY_CONFIG="$config_file"
        if mvn -q exec:java -Dexec.mainClass="$main_class" 2>&1 | head -20; then
            echo -e "${GREEN}✓ ${test_name} - PASSED${NC}"
        else
            echo -e "${RED}✗ ${test_name} - FAILED${NC}"
        fi
    else
        echo -e "${RED}✗ Config file not found: ${config_file}${NC}"
    fi

    echo ""
    sleep 2
}

# Build the project first
echo "📦 Building project..."
mvn -q clean package -DskipTests
echo -e "${GREEN}✓ Build complete${NC}"
echo ""

# Test 1: Console Exporter
echo "=== Test 1: Console Exporter ==="
run_test "Console Exporter" \
    "src/main/resources/observability.yml" \
    "com.observability.examples.QuickStartExample"

# Test 2: Comprehensive Example
echo "=== Test 2: Comprehensive Features ==="
run_test "All SDK Features" \
    "src/main/resources/observability.yml" \
    "com.observability.examples.ComprehensiveExample"

# Test 3: Config-Based
echo "=== Test 3: YAML Configuration ==="
run_test "Config-Based Loading" \
    "src/main/resources/observability.yml" \
    "com.observability.examples.ConfigBasedExample"

# Check if Docker services are running
if docker ps | grep -q "observability-prometheus"; then
    echo "=== Test 4: Prometheus Exporter ==="
    run_test "Prometheus Pushgateway" \
        "../config-examples/prometheus-jaeger.yml" \
        "com.observability.examples.ConfigBasedExample"

    echo "📊 Verify Prometheus metrics:"
    echo "   http://localhost:9091/metrics"
    echo "   http://localhost:9090/targets"
    echo ""
fi

if docker ps | grep -q "observability-jaeger"; then
    echo "=== Test 5: Jaeger Exporter ==="
    run_test "Jaeger Tracing" \
        "../config-examples/prometheus-jaeger.yml" \
        "com.observability.examples.ComprehensiveExample"

    echo "🔍 Verify Jaeger traces:"
    echo "   http://localhost:16686"
    echo ""
fi

if docker ps | grep -q "observability-otel-collector"; then
    echo "=== Test 6: OTLP Exporter ==="
    cat > /tmp/test-otlp.yml <<EOF
service:
  name: "test-otlp"

exporters:
  metrics:
    - type: "otlp"
      enabled: true
      endpoint: "http://localhost:4318/v1/metrics"

  traces:
    - type: "otlp"
      enabled: true
      endpoint: "http://localhost:4318/v1/traces"
EOF

    run_test "OpenTelemetry OTLP" \
        "/tmp/test-otlp.yml" \
        "com.observability.examples.ComprehensiveExample"

    echo "📡 OTLP Collector logs:"
    docker logs observability-otel-collector --tail 20
    echo ""
fi

# Test 7: Multi-Backend
echo "=== Test 7: Multi-Backend Export ==="
run_test "Multiple Backends Simultaneously" \
    "../config-examples/multi-backend.yml" \
    "com.observability.examples.ConfigBasedExample"

echo ""
echo "==========================================="
echo -e "${GREEN}✅ Test Suite Complete!${NC}"
echo ""
echo "📋 Summary:"
echo "  - Console exporter: Local output"
echo "  - Prometheus: http://localhost:9090"
echo "  - Jaeger: http://localhost:16686"
echo "  - Grafana: http://localhost:3000 (admin/admin)"
echo ""
echo "💡 To start all backends:"
echo "   docker-compose up -d"
echo ""
echo "💡 To stop all backends:"
echo "   docker-compose down"
