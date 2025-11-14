package com.observability.examples;

import com.observability.api.logs.Logger;
import com.observability.api.metrics.Counter;
import com.observability.api.trace.Tracer;
import com.observability.core.ObservabilitySDKImpl;

/**
 * Quick start example showing minimal SDK usage.
 */
public class QuickStartExample {

    public static void main(String[] args) {
        // 1. Initialize SDK
        var sdk = ObservabilitySDKImpl.builder()
            .setServiceName("my-service")
            .build();

        // 2. Get a metric counter
        Counter requests = sdk.getMetricRegistry()
            .counter("requests", "Total requests");

        // 3. Get a tracer
        Tracer tracer = sdk.getTracerProvider()
            .getTracer("my-app");

        // 4. Get a logger
        Logger logger = sdk.getLoggerProvider()
            .getLogger(QuickStartExample.class);

        // 5. Use them!
        tracer.withSpan("my-operation", () -> {
            requests.increment();
            logger.info("Processing request");

            // Your business logic here
            System.out.println("Hello from instrumented code!");
        });

        // 6. Cleanup
        sdk.shutdown();
    }
}
