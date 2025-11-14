package com.observability.examples;

import com.observability.api.common.Attributes;
import com.observability.api.logs.Logger;
import com.observability.api.metrics.Counter;
import com.observability.api.metrics.Gauge;
import com.observability.api.metrics.Histogram;
import com.observability.api.metrics.MetricRegistry;
import com.observability.api.trace.Span;
import com.observability.api.trace.SpanKind;
import com.observability.api.trace.Tracer;
import com.observability.core.ObservabilitySDKImpl;
import com.observability.exporters.console.ConsoleMetricExporter;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive example demonstrating all SDK features:
 * - Metrics (Counter, Gauge, Histogram)
 * - Distributed Tracing
 * - Structured Logging
 * - Context Propagation
 * - Virtual Threads for async operations
 */
public class ComprehensiveExample {

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 Observability SDK - Comprehensive Example (Java 21)\n");

        // Initialize SDK
        var sdk = ObservabilitySDKImpl.builder()
            .setServiceName("demo-service")
            .setServiceVersion("1.0.0")
            .build();

        // Get components
        MetricRegistry metricRegistry = sdk.getMetricRegistry();
        Tracer tracer = sdk.getTracerProvider().getTracer("demo-app", "1.0.0");
        Logger logger = sdk.getLoggerProvider().getLogger(ComprehensiveExample.class);

        // ============== METRICS EXAMPLE ==============
        System.out.println("📊 === Metrics Example ===\n");

        // Counter: Track total requests
        Counter requestCounter = metricRegistry.counter(
            "http.requests.total",
            "Total HTTP requests",
            "requests"
        );

        // Gauge: Track active connections (with callback)
        Random random = new Random();
        Gauge activeConnections = metricRegistry.gauge(
            "http.connections.active",
            "Active HTTP connections",
            "connections",
            () -> (double) random.nextInt(10, 50)
        );

        // Histogram: Track request durations
        Histogram requestDuration = metricRegistry.histogram(
            "http.request.duration",
            "HTTP request duration",
            "ms"
        );

        // Simulate some activity
        for (int i = 0; i < 100; i++) {
            requestCounter.increment();
            requestDuration.record(random.nextDouble(10, 500));
        }

        // Export metrics
        var metricExporter = new ConsoleMetricExporter();
        metricExporter.export(metricRegistry.getMetrics());

        System.out.println();

        // ============== TRACING EXAMPLE ==============
        System.out.println("🔍 === Distributed Tracing Example ===\n");

        // Create a parent span for the entire operation
        try (Span parentSpan = tracer.spanBuilder("process-order")
            .setSpanKind(SpanKind.SERVER)
            .setAttribute("order.id", "ORDER-12345")
            .setAttribute("customer.id", "CUST-789")
            .startSpan()) {

            logger.info("Processing order",
                Attributes.of("orderId", "ORDER-12345")
            );

            // Child span for database operation
            try (Span dbSpan = tracer.spanBuilder("database-query")
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute("db.system", "postgresql")
                .setAttribute("db.operation", "SELECT")
                .startSpan()) {

                dbSpan.addEvent("query.start");

                // Simulate database work
                Thread.sleep(50);

                dbSpan.addEvent("query.complete",
                    Attributes.of("rows.returned", 5)
                );

                logger.debug("Database query completed");
            }

            // Child span for external API call
            try (Span apiSpan = tracer.spanBuilder("external-api-call")
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute("http.method", "POST")
                .setAttribute("http.url", "https://api.payment.com/charge")
                .startSpan()) {

                apiSpan.addEvent("request.sent");

                // Simulate API call
                Thread.sleep(100);

                apiSpan.setStatus(Span.SpanStatus.OK);
                logger.info("Payment processed successfully");
            }

            // Simulate an error scenario
            try (Span errorSpan = tracer.spanBuilder("validate-inventory")
                .startSpan()) {

                try {
                    throw new RuntimeException("Item out of stock");
                } catch (Exception e) {
                    errorSpan.recordException(e);
                    errorSpan.setStatus(Span.SpanStatus.ERROR, "Inventory validation failed");
                    logger.error("Inventory validation failed", e);
                }
            }

            parentSpan.setStatus(Span.SpanStatus.OK);
            logger.info("Order processing completed");
        }

        System.out.println();

        // ============== VIRTUAL THREADS EXAMPLE ==============
        System.out.println("⚡ === Virtual Threads Example ===\n");

        // Demonstrate concurrent operations using virtual threads
        var executor = sdk.getExecutor();

        // Submit multiple concurrent tasks
        var futures = java.util.stream.IntStream.range(0, 10)
            .mapToObj(i -> executor.submit(() -> {
                try (Span span = tracer.spanBuilder("concurrent-task-" + i)
                    .setAttribute("task.id", i)
                    .startSpan()) {

                    // Simulate work
                    Thread.sleep(random.nextInt(50, 150));

                    Counter taskCounter = metricRegistry.counter(
                        "tasks.completed",
                        "Completed tasks"
                    );
                    taskCounter.increment();

                    logger.info("Task completed",
                        Attributes.of("taskId", i)
                    );

                    span.setStatus(Span.SpanStatus.OK);
                }
                return i;
            }))
            .toList();

        // Wait for all tasks
        for (var future : futures) {
            future.get();
        }

        System.out.println("✅ All concurrent tasks completed!\n");

        // ============== CONTEXT PROPAGATION EXAMPLE ==============
        System.out.println("🔗 === Context Propagation Example ===\n");

        tracer.withSpan("parent-operation", () -> {
            logger.info("In parent operation");

            // Nested operation that inherits context
            tracer.withSpan("child-operation", () -> {
                logger.info("In child operation - context is propagated!");

                // Even deeper nesting
                tracer.withSpan("grandchild-operation", () -> {
                    logger.info("In grandchild operation - still propagated!");
                });
            });
        });

        // ============== FINAL METRICS ==============
        System.out.println("\n📈 === Final Metrics Summary ===\n");
        metricExporter.export(metricRegistry.getMetrics());

        // Cleanup
        sdk.shutdown();
        System.out.println("\n✅ SDK shutdown complete");
    }
}
