package com.observability.examples;

import com.observability.api.common.Attributes;
import com.observability.api.logs.Logger;
import com.observability.api.metrics.Counter;
import com.observability.api.metrics.Histogram;
import com.observability.api.trace.Tracer;
import com.observability.core.ObservabilitySDKImpl;

import java.util.Random;

/**
 * Test all configured exporters by generating observability data.
 *
 * This example works with ANY configuration - just change the YAML!
 *
 * Usage:
 * 1. Configure exporters in observability.yml
 * 2. Start backends (if testing locally): docker-compose up -d
 * 3. Run this example
 * 4. Verify data in your observability platform
 *
 * Supports:
 * - Console (stdout)
 * - Prometheus (http://localhost:9090)
 * - Jaeger (http://localhost:16686)
 * - Grafana Cloud
 * - Datadog
 * - New Relic
 * - Any OTLP-compatible backend
 */
public class AllExportersTest {

    private static final Random random = new Random();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("🧪 Testing All Configured Exporters");
        System.out.println("====================================\n");

        // Load SDK from YAML configuration
        var sdk = ObservabilitySDKImpl.fromConfig();

        System.out.println("\n🎯 Generating test data...\n");

        // Get SDK components
        var metricRegistry = sdk.getMetricRegistry();
        var tracer = sdk.getTracerProvider().getTracer("test-app", "1.0.0");
        var logger = sdk.getLoggerProvider().getLogger(AllExportersTest.class);

        // Create test metrics
        Counter totalRequests = metricRegistry.counter(
            "test.requests.total",
            "Total test requests",
            "requests"
        );

        Counter successfulRequests = metricRegistry.counter(
            "test.requests.successful",
            "Successful test requests",
            "requests"
        );

        Counter failedRequests = metricRegistry.counter(
            "test.requests.failed",
            "Failed test requests",
            "requests"
        );

        Histogram requestDuration = metricRegistry.histogram(
            "test.request.duration",
            "Test request duration",
            "ms"
        );

        Histogram responseSize = metricRegistry.histogram(
            "test.response.size",
            "Test response size",
            "bytes"
        );

        // Generate test data
        for (int i = 0; i < 100; i++) {
            final int requestNum = i;

            // Create a trace for each request
            tracer.withSpan("test-request-" + requestNum, () -> {
                // Simulate request processing
                totalRequests.increment();

                // Random duration between 10-500ms
                double duration = random.nextDouble() * 490 + 10;
                requestDuration.record(duration);

                // Random response size between 100-10000 bytes
                double size = random.nextDouble() * 9900 + 100;
                responseSize.record(size);

                // 90% success, 10% failure
                boolean success = random.nextDouble() < 0.9;

                if (success) {
                    successfulRequests.increment();

                    logger.info("Request processed successfully",
                        Attributes.builder()
                            .put("requestId", "REQ-" + requestNum)
                            .put("duration", duration)
                            .put("responseSize", size)
                            .put("status", "success")
                            .build()
                    );
                } else {
                    failedRequests.increment();

                    logger.error("Request failed",
                        Attributes.builder()
                            .put("requestId", "REQ-" + requestNum)
                            .put("duration", duration)
                            .put("status", "error")
                            .put("errorType", "timeout")
                            .build()
                    );
                }

                // Nested span for database operation
                tracer.withSpan("database-query", () -> {
                    try {
                        Thread.sleep(random.nextInt(50));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                // Nested span for external API call
                tracer.withSpan("external-api-call", () -> {
                    try {
                        Thread.sleep(random.nextInt(100));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });

            // Progress indicator
            if ((i + 1) % 10 == 0) {
                System.out.printf("  Progress: %d/100 requests completed\n", i + 1);
            }

            Thread.sleep(50); // Throttle to avoid overwhelming exporters
        }

        System.out.println("\n✅ Test data generation complete!\n");

        // Export final metrics
        System.out.println("📤 Exporting final metrics to all configured backends...\n");

        if (sdk instanceof ObservabilitySDKImpl impl) {
            var metrics = metricRegistry.getMetrics();

            // Export to all metric exporters
            impl.getMetricExporters().forEach(exporter -> {
                System.out.println("  → Exporting to " + exporter.getClass().getSimpleName());
                exporter.export(metrics);
            });
        }

        // Print summary
        System.out.println("\n📊 Test Summary:");
        System.out.println("  Total Requests: 100");
        System.out.println("  Successful: ~90");
        System.out.println("  Failed: ~10");
        System.out.println("  Spans Created: ~300 (parent + 2 children each)");
        System.out.println("  Log Entries: ~100");

        System.out.println("\n🔍 Verification:");
        System.out.println("  Check your configured backends to see the data!");
        System.out.println();
        System.out.println("  Console: Check stdout above");
        System.out.println("  Prometheus: http://localhost:9090/graph");
        System.out.println("  Prometheus Pushgateway: http://localhost:9091/metrics");
        System.out.println("  Jaeger: http://localhost:16686");
        System.out.println("  Grafana: http://localhost:3000");
        System.out.println("  Grafana Cloud: https://grafana.com");
        System.out.println("  Datadog: https://app.datadoghq.com");
        System.out.println("  New Relic: https://one.newrelic.com");

        System.out.println("\n💡 Query Examples:");
        System.out.println("  Prometheus: sum(rate(test_requests_total[1m]))");
        System.out.println("  Jaeger: Search for service 'test-app'");

        // Cleanup
        Thread.sleep(2000); // Give exporters time to flush
        sdk.shutdown();

        System.out.println("\n✅ All done! Check your observability platforms.\n");
    }
}
