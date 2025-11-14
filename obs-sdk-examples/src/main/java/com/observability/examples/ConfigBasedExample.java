package com.observability.examples;

import com.observability.api.common.Attributes;
import com.observability.api.logs.Logger;
import com.observability.api.metrics.Counter;
import com.observability.api.metrics.Histogram;
import com.observability.api.trace.Tracer;
import com.observability.core.ObservabilitySDKImpl;

/**
 * Example demonstrating zero-code configuration.
 * Just drop observability.yml and the SDK auto-configures all exporters!
 *
 * Usage:
 * 1. Copy observability.yml to classpath or current directory
 * 2. Enable/disable exporters in YAML
 * 3. Run this example - no code changes needed!
 */
public class ConfigBasedExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("🚀 Zero-Code Configuration Example\n");
        System.out.println("Loading configuration from YAML...\n");

        // ONE LINE - SDK auto-discovers and loads config!
        var sdk = ObservabilitySDKImpl.fromConfig();

        System.out.println("\n📊 Starting observability...\n");

        // Get components (configured from YAML)
        var metricRegistry = sdk.getMetricRegistry();
        var tracer = sdk.getTracerProvider().getTracer("config-example");
        var logger = sdk.getLoggerProvider().getLogger(ConfigBasedExample.class);

        // Create metrics
        Counter requests = metricRegistry.counter(
            "http.requests",
            "Total HTTP requests",
            "requests"
        );

        Histogram latency = metricRegistry.histogram(
            "http.latency",
            "HTTP request latency",
            "ms"
        );

        // Simulate application traffic
        System.out.println("Simulating application traffic...\n");

        for (int i = 0; i < 50; i++) {
            tracer.withSpan("handle-request", () -> {
                // Record metrics
                requests.increment();
                latency.record(Math.random() * 200 + 50);

                // Log
                logger.info("Processing request",
                    Attributes.builder()
                        .put("requestId", "REQ-" + i)
                        .put("userId", "USER-" + (i % 10))
                        .build()
                );
            });

            Thread.sleep(100);
        }

        System.out.println("\n✅ All requests processed!");
        System.out.println("\n📤 Exporters automatically sent data to configured backends:");
        System.out.println("   - Check your observability platform!");
        System.out.println("   - All exporters from YAML config were used\n");

        // Export final metrics
        if (sdk instanceof ObservabilitySDKImpl impl) {
            impl.getMetricExporters().forEach(exporter -> {
                exporter.export(metricRegistry.getMetrics());
            });
        }

        // Cleanup
        sdk.shutdown();

        System.out.println("\n💡 To change exporters:");
        System.out.println("   1. Edit observability.yml");
        System.out.println("   2. Enable/disable exporters");
        System.out.println("   3. Restart - NO CODE CHANGES!");
    }
}
