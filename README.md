# Observability SDK for Java 21+

An enterprise-grade, highly extensible observability SDK built with Java 21 features for modern applications.

## 🌟 Features

### Core Capabilities
- **📊 Metrics**: Counters, Gauges, Histograms with thread-safe implementations
- **🔍 Distributed Tracing**: Full-featured tracing with context propagation
- **📝 Structured Logging**: Correlated logging with trace context
- **🔗 Context Propagation**: Automatic context handling using Java 21's `ScopedValue`
- **⚡ Virtual Threads**: High-performance async operations with Java 21 virtual threads
- **🔌 Extensible Exporters**: Pluggable backend support (console, OpenTelemetry, custom)

### Design Principles
- **Clean Architecture**: Clear separation of API and implementation
- **Type Safety**: Leveraging Java 21 sealed interfaces and records
- **Thread Safety**: Lock-free and concurrent data structures
- **Zero Dependencies**: Core module has no external dependencies
- **Performance**: Optimized for high-throughput scenarios

## 🏗️ Architecture

```
obs-sdk-parent/
├── obs-sdk-api/          # Core API contracts and abstractions
├── obs-sdk-core/         # Implementation with Java 21 features
├── obs-sdk-exporters/    # Pluggable exporters (console, etc.)
└── obs-sdk-examples/     # Usage examples and demos
```

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.8+

### Build

```bash
mvn clean install
```

### Basic Usage

```java
import com.observability.core.ObservabilitySDKImpl;

// Initialize SDK
var sdk = ObservabilitySDKImpl.builder()
    .setServiceName("my-service")
    .setServiceVersion("1.0.0")
    .build();

// Create a counter
var requests = sdk.getMetricRegistry()
    .counter("http.requests", "Total HTTP requests");

// Create spans with automatic context propagation
var tracer = sdk.getTracerProvider().getTracer("my-app");
tracer.withSpan("my-operation", () -> {
    requests.increment();
    // Your code here
});

// Structured logging
var logger = sdk.getLoggerProvider().getLogger(MyClass.class);
logger.info("Request processed",
    Attributes.of("userId", "123"));

// Cleanup
sdk.shutdown();
```

## 📊 Metrics

### Counter
Monotonically increasing value for counting events.

```java
Counter counter = metricRegistry.counter(
    "requests.total",
    "Total requests",
    "requests"
);

counter.increment();
counter.increment(5.0);
```

### Gauge
Point-in-time value that can go up or down.

```java
// Direct value
Gauge gauge = metricRegistry.gauge(
    "memory.used",
    "Used memory",
    "bytes"
);
gauge.set(1024.0);

// Callback-based
Gauge activeConnections = metricRegistry.gauge(
    "connections.active",
    "Active connections",
    "connections",
    () -> getActiveConnectionCount()
);
```

### Histogram
Statistical distribution of values.

```java
Histogram histogram = metricRegistry.histogram(
    "request.duration",
    "Request duration",
    "ms"
);

histogram.record(145.5);

// Get statistics
var snapshot = histogram.snapshot();
System.out.println("Mean: " + snapshot.mean());
System.out.println("P95: " + snapshot.percentileValues()[1]);
```

## 🔍 Distributed Tracing

### Creating Spans

```java
Tracer tracer = tracerProvider.getTracer("my-service");

// Automatic span management with try-with-resources
try (Span span = tracer.spanBuilder("database-query")
    .setSpanKind(SpanKind.CLIENT)
    .setAttribute("db.system", "postgresql")
    .startSpan()) {

    // Your code
    span.addEvent("query.start");

    // Nested spans automatically inherit context
    try (Span childSpan = tracer.spanBuilder("parse-results")
        .startSpan()) {
        // Process results
    }

    span.setStatus(Span.SpanStatus.OK);
}
```

### Error Handling

```java
try (Span span = tracer.spanBuilder("risky-operation").startSpan()) {
    try {
        riskyOperation();
    } catch (Exception e) {
        span.recordException(e);
        span.setStatus(Span.SpanStatus.ERROR, "Operation failed");
        throw e;
    }
}
```

## 📝 Structured Logging

```java
Logger logger = loggerProvider.getLogger(MyClass.class);

// Simple logging
logger.info("User logged in");

// With attributes
logger.info("Request processed",
    Attributes.builder()
        .put("userId", "123")
        .put("duration", 145.5)
        .build()
);

// With exceptions
logger.error("Failed to process", throwable);

// Logs automatically include trace context
tracer.withSpan("operation", () -> {
    logger.info("This log is correlated with the span!");
});
```

## 🔗 Context Propagation

Context is automatically propagated using Java 21's `ScopedValue`:

```java
tracer.withSpan("parent", () -> {
    // Context is available here

    tracer.withSpan("child", () -> {
        // Child span automatically has parent context

        // Works with virtual threads!
        executor.submit(() -> {
            // Context is propagated to virtual threads
            logger.info("Still have context!");
        });
    });
});
```

## ⚡ Virtual Threads

The SDK leverages Java 21 virtual threads for high-performance async operations:

```java
var executor = sdk.getExecutor();

// Launch thousands of concurrent operations efficiently
for (int i = 0; i < 10000; i++) {
    executor.submit(() -> {
        tracer.withSpan("async-task", () -> {
            // High-concurrency operations
            // Context is automatically propagated!
        });
    });
}
```

## 🔌 Exporters

### Console Exporter (Built-in)

```java
var exporter = new ConsoleMetricExporter();
exporter.export(metricRegistry.getMetrics());
```

### Custom Exporters

Implement the `Exporter` interface:

```java
public class MyExporter implements MetricExporter {
    @Override
    public ExportResult export(Collection<Metric> metrics) {
        // Send to your backend
        return ExportResult.SUCCESS;
    }

    @Override
    public ExportResult flush() {
        // Flush any buffered data
        return ExportResult.SUCCESS;
    }

    @Override
    public void shutdown() {
        // Cleanup resources
    }
}
```

## 🎯 Java 21 Features Used

- **Virtual Threads**: High-performance concurrency
- **ScopedValue**: Thread-safe context propagation
- **Records**: Immutable data classes
- **Sealed Interfaces**: Type-safe metric hierarchy
- **Pattern Matching**: Clean type checks
- **Text Blocks**: Readable multi-line strings

## 🏢 Enterprise Features

- **Thread Safety**: All components are thread-safe
- **Performance**: Lock-free data structures where possible
- **Extensibility**: Plugin architecture for custom components
- **Zero Dependencies**: Core module has no external dependencies
- **Production Ready**: Designed for high-throughput scenarios

## 📚 Examples

Run the comprehensive example:

```bash
cd obs-sdk-examples
mvn exec:java -Dexec.mainClass="com.observability.examples.ComprehensiveExample"
```

Run the quick start:

```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.QuickStartExample"
```

## 🧪 Testing

```bash
mvn test
```

## 📖 API Documentation

Generate JavaDocs:

```bash
mvn javadoc:javadoc
```

## 🤝 Contributing

This is an enterprise-grade SDK designed for extensibility. Key extension points:

- **Metrics**: Implement `Metric` interface
- **Exporters**: Implement `Exporter<T>` interface
- **Context Storage**: Extend `ContextManager`
- **Samplers**: Add custom sampling strategies

## 📄 License

Enterprise SDK - All Rights Reserved

## 🎓 Architecture Highlights

### Separation of Concerns
- **API**: Pure interfaces and contracts
- **Core**: Implementation details
- **Exporters**: Backend integrations

### Thread Safety
- Lock-free counters using `DoubleAdder`
- Concurrent collections for registries
- Immutable data structures

### Performance
- Virtual threads for high concurrency
- Batching for efficient exports
- Lock-free operations where possible

### Extensibility
- Strategy pattern for exporters
- Factory pattern for providers
- Builder pattern for configuration

---

Built with ❤️ using Java 21 features for modern observability.
