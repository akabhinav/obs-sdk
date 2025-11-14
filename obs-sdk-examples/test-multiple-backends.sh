#!/bin/bash

# Test Multiple Backends Script
# Tests the SDK with various observability platforms

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Observability SDK - Backend Test Suite   ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
echo ""

# Function to test a backend
test_backend() {
    local name=$1
    local config=$2
    local test_class=${3:-"com.observability.examples.AllExportersTest"}

    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}Testing: ${name}${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

    if [ ! -f "$config" ]; then
        echo -e "${RED}✗ Config file not found: ${config}${NC}"
        return 1
    fi

    export OBSERVABILITY_CONFIG="$config"

    if mvn -q exec:java -Dexec.mainClass="$test_class" 2>&1 | head -30; then
        echo -e "${GREEN}✓ ${name} - PASSED${NC}"
        return 0
    else
        echo -e "${RED}✗ ${name} - FAILED${NC}"
        return 1
    fi
}

# Results tracking
declare -A results
total=0
passed=0
failed=0

# Test 1: Console (always works)
echo ""
echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo -e "${BLUE}  LOCAL BACKENDS (No signup required)     ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo ""

total=$((total + 1))
if test_backend "Console Exporter" "src/main/resources/observability.yml" "com.observability.examples.QuickStartExample"; then
    passed=$((passed + 1))
    results["console"]="✓"
else
    failed=$((failed + 1))
    results["console"]="✗"
fi

sleep 2

# Test 2: Prometheus (if Docker is running)
if docker ps | grep -q "obs-prometheus"; then
    total=$((total + 1))
    echo ""
    if test_backend "Prometheus" "../config-examples/prometheus-jaeger.yml"; then
        passed=$((passed + 1))
        results["prometheus"]="✓"
        echo -e "${GREEN}→ Verify: http://localhost:9090/graph${NC}"
        echo -e "${GREEN}→ Query: test_requests_total${NC}"
    else
        failed=$((failed + 1))
        results["prometheus"]="✗"
    fi
    sleep 2
else
    echo -e "${YELLOW}⊘ Prometheus - Skipped (Docker not running)${NC}"
    echo -e "${YELLOW}  Start with: docker-compose up -d${NC}"
fi

# Test 3: Jaeger (if Docker is running)
if docker ps | grep -q "obs-jaeger"; then
    total=$((total + 1))
    echo ""
    if test_backend "Jaeger" "../config-examples/prometheus-jaeger.yml" "com.observability.examples.ComprehensiveExample"; then
        passed=$((passed + 1))
        results["jaeger"]="✓"
        echo -e "${GREEN}→ Verify: http://localhost:16686${NC}"
    else
        failed=$((failed + 1))
        results["jaeger"]="✗"
    fi
    sleep 2
else
    echo -e "${YELLOW}⊘ Jaeger - Skipped (Docker not running)${NC}"
fi

# Cloud Backends
echo ""
echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo -e "${BLUE}  CLOUD BACKENDS (Require credentials)    ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo ""

# Test cloud backends if configs exist and have credentials
cloud_backends=(
    "Grafana Cloud:../config-examples/grafana-cloud.yml"
    "Datadog:../config-examples/datadog.yml"
    "New Relic:../config-examples/newrelic.yml"
    "Honeycomb:../config-examples/honeycomb.yml"
    "Lightstep:../config-examples/lightstep.yml"
    "Elastic APM:../config-examples/elastic-apm.yml"
)

for backend_config in "${cloud_backends[@]}"; do
    IFS=: read -r name config <<< "$backend_config"

    if [ -f "$config" ]; then
        # Check if config has actual credentials (not placeholder)
        if grep -q "YOUR_.*_HERE\|your-.*-here" "$config"; then
            echo -e "${YELLOW}⊘ ${name} - Skipped (credentials not configured)${NC}"
            echo -e "${YELLOW}  Edit: ${config}${NC}"
        else
            total=$((total + 1))
            echo ""
            if test_backend "$name" "$config"; then
                passed=$((passed + 1))
                results["${name}"]="✓"
            else
                failed=$((failed + 1))
                results["${name}"]="✗"
            fi
            sleep 2
        fi
    fi
done

# Multi-backend test
if docker ps | grep -q "obs-prometheus"; then
    total=$((total + 1))
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════${NC}"
    echo -e "${BLUE}  MULTI-BACKEND TEST                      ${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════${NC}"
    echo ""

    if test_backend "Multi-Backend (All simultaneously)" "../config-examples/multi-backend.yml"; then
        passed=$((passed + 1))
        results["multi-backend"]="✓"
        echo -e "${GREEN}→ Data sent to ALL configured backends!${NC}"
    else
        failed=$((failed + 1))
        results["multi-backend"]="✗"
    fi
fi

# Summary
echo ""
echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo -e "${BLUE}  TEST SUMMARY                             ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo ""

echo -e "Total Tests:  ${total}"
echo -e "${GREEN}Passed:       ${passed}${NC}"
if [ $failed -gt 0 ]; then
    echo -e "${RED}Failed:       ${failed}${NC}"
else
    echo -e "Failed:       ${failed}"
fi

echo ""
echo -e "${BLUE}Results by Backend:${NC}"
for backend in "${!results[@]}"; do
    if [ "${results[$backend]}" = "✓" ]; then
        echo -e "  ${GREEN}${results[$backend]}${NC} $backend"
    else
        echo -e "  ${RED}${results[$backend]}${NC} $backend"
    fi
done

echo ""
echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo ""

if [ $failed -eq 0 ]; then
    echo -e "${GREEN}✅ All tests passed!${NC}"
    echo ""
    echo -e "${BLUE}Next steps:${NC}"
    echo "  1. Check dashboards for your data"
    echo "  2. Configure cloud backends for production"
    echo "  3. Customize metrics for your use case"
else
    echo -e "${YELLOW}⚠ Some tests failed or were skipped${NC}"
    echo ""
    echo -e "${BLUE}To enable more tests:${NC}"
    echo "  1. Start Docker: docker-compose up -d"
    echo "  2. Configure cloud credentials in config-examples/"
    echo "  3. Run again: ./test-multiple-backends.sh"
fi

echo ""
echo -e "${BLUE}Documentation:${NC}"
echo "  - BACKENDS.md - All 40+ supported backends"
echo "  - LOCAL-TESTING.md - Local testing guide"
echo "  - CONFIGURATION.md - Configuration reference"
echo ""

exit $failed
