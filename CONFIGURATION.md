# Configuration Guide

## Zero-Code Exporter Configuration

The Observability SDK supports **configuration-driven exporters** - no code changes needed!

## Supported Platforms

### ✅ Currently Supported (40+ backends via OTLP)

#### Universal Protocol
- **OpenTelemetry (OTLP)** - Works with 40+ backends including:
  - Grafana Cloud
  - Datadog
  - New Relic
  - Honeycomb
  - Lightstep
  - Elastic APM
  - AWS X-Ray (via Collector)
  - Google Cloud Operations
  - Azure Monitor
  - And many more...

#### Metrics Platforms
- **Prometheus** - Open-source metrics (push/pull)
- **Grafana Cloud** - Cloud-native observability
- **Datadog** - Full-stack monitoring
- **New Relic** - Application performance monitoring
- **AWS CloudWatch** - AWS native monitoring
- **Console** - Development/debugging

#### Distributed Tracing
- **Jaeger** - OpenTracing compatible
- **Zipkin** - Distributed tracing
- **All OTLP-compatible backends**

#### Logging
- **Console** - Standard output
- **All OTLP-compatible backends**

## Quick Start

### 1. Drop Configuration File

Create `observability.yml` in your project root or classpath:

```yaml
service:
  name: "my-service"
  version: "1.0.0"

exporters:
  metrics:
    - type: "console"
      enabled: true

    - type: "otlp"
      enabled: false
      endpoint: "http://localhost:4318/v1/metrics"
```

### 2. Use in Code

```java
// ONE LINE - auto-loads from YAML!
var sdk = ObservabilitySDKImpl.fromConfig();

// Use SDK normally
Counter counter = sdk.getMetricRegistry().counter("requests", "Total requests");
counter.increment();
```

### 3. Change Backends - NO CODE CHANGES!

Just edit `observability.yml`:

```yaml
exporters:
  metrics:
    - type: "grafana-cloud"  # Changed from console
      enabled: true
      endpoint: "https://otlp-gateway.grafana.net/otlp"
      headers:
        Authorization: "Basic your-credentials"
```

Restart your app - that's it!

## Configuration Reference

### Service Configuration

```yaml
service:
  name: "my-service"           # Required
  version: "1.0.0"             # Optional
  environment: "production"     # Optional
  attributes:                   # Optional custom attributes
    team: "platform"
    region: "us-east-1"
```

### Exporter Configuration

Each exporter supports:

```yaml
exporters:
  metrics:
    - type: "exporter-type"      # Required: console, otlp, prometheus, etc.
      enabled: true              # Optional: default true
      endpoint: "http://..."     # Optional: platform-specific
      headers:                    # Optional: authentication headers
        api-key: "your-key"
      timeout_ms: 10000          # Optional: request timeout
      batch_size: 100            # Optional: batch size
      batch_timeout_ms: 5000     # Optional: batch timeout
      config:                     # Optional: platform-specific config
        custom_setting: "value"
```

## Platform-Specific Examples

### Grafana Cloud

```yaml
exporters:
  metrics:
    - type: "grafana-cloud"
      endpoint: "https://otlp-gateway-prod-us-central-0.grafana.net/otlp/v1/metrics"
      headers:
        Authorization: "Basic <base64-encoded-instance-id:api-key>"

  traces:
    - type: "grafana-cloud"
      endpoint: "https://otlp-gateway-prod-us-central-0.grafana.net/otlp/v1/traces"
      headers:
        Authorization: "Basic <base64-encoded-instance-id:api-key>"
```

### Datadog

```yaml
exporters:
  metrics:
    - type: "datadog"
      endpoint: "https://api.datadoghq.com"
      headers:
        DD-API-KEY: "your-datadog-api-key"

  traces:
    - type: "datadog"
      endpoint: "https://trace.agent.datadoghq.com"
      headers:
        DD-API-KEY: "your-datadog-api-key"
```

### Prometheus + Jaeger

```yaml
exporters:
  metrics:
    - type: "prometheus"
      endpoint: "http://prometheus-pushgateway:9091/metrics/job/my-service"

  traces:
    - type: "jaeger"
      endpoint: "http://jaeger-collector:14268/api/traces"
```

### AWS CloudWatch

```yaml
exporters:
  metrics:
    - type: "cloudwatch"
      config:
        region: "us-east-1"
        namespace: "MyApp/Metrics"
```

### New Relic

```yaml
exporters:
  metrics:
    - type: "newrelic"
      endpoint: "https://otlp.nr-data.net:4318"
      headers:
        api-key: "your-newrelic-license-key"
```

### Multi-Backend (Send to Multiple Platforms)

```yaml
exporters:
  metrics:
    - type: "console"
      enabled: true

    - type: "prometheus"
      enabled: true
      endpoint: "http://prometheus:9091/metrics/job/my-service"

    - type: "grafana-cloud"
      enabled: true
      endpoint: "https://otlp-gateway.grafana.net/otlp"
      headers:
        Authorization: "Basic your-credentials"

    - type: "datadog"
      enabled: true
      headers:
        DD-API-KEY: "your-api-key"
```

## Configuration Loading Priority

The SDK searches for configuration in this order:

1. **Environment Variable**: `OBSERVABILITY_CONFIG=/path/to/config.yml`
2. **System Property**: `-Dobservability.config=/path/to/config.yml`
3. **Current Directory**: `./observability.yml` or `./observability.yaml`
4. **Config Directory**: `./config/observability.yml`
5. **Home Directory**: `~/.observability/config.yml`
6. **Classpath**: `observability.yml` in classpath

## Programmatic Configuration

You can also load config programmatically:

```java
// From file path
var sdk = ObservabilitySDKImpl.fromConfig(Paths.get("/path/to/config.yml"));

// From config object
ObservabilityConfig config = new ConfigLoader().loadConfig();
var sdk = ObservabilitySDKImpl.fromConfig(config);
```

## Supported Exporter Types

| Type | Platforms | Signals |
|------|-----------|---------|
| `console` | Development | Metrics, Traces, Logs |
| `otlp` | 40+ backends | Metrics, Traces, Logs |
| `otlp-http` | Same as otlp | Metrics, Traces, Logs |
| `otlp-grpc` | Same as otlp | Metrics, Traces |
| `prometheus` | Prometheus | Metrics |
| `prometheus-pushgateway` | Prometheus | Metrics |
| `grafana-cloud` | Grafana Cloud | Metrics, Traces, Logs |
| `datadog` | Datadog | Metrics, Traces, Logs |
| `newrelic` | New Relic | Metrics, Traces, Logs |
| `cloudwatch` | AWS CloudWatch | Metrics, Logs |
| `jaeger` | Jaeger | Traces |
| `zipkin` | Zipkin | Traces |

## Environment-Specific Configs

Create different configs for different environments:

```bash
# Development
observability-dev.yml

# Staging
observability-staging.yml

# Production
observability-prod.yml
```

Load with environment variable:

```bash
export OBSERVABILITY_CONFIG=observability-prod.yml
java -jar your-app.jar
```

## Best Practices

1. **Start with Console**: Test locally with console exporter
2. **Use OTLP**: Maximum compatibility with backends
3. **Multi-Backend**: Send to multiple platforms for redundancy
4. **Environment-Specific**: Different configs for dev/staging/prod
5. **Secure Credentials**: Use environment variables for API keys

```yaml
headers:
  api-key: "${OBSERVABILITY_API_KEY}"  # Future feature
```

## Troubleshooting

### Exporter Not Loading

Check SDK initialization output:
```
✓ Loaded metric exporter: console
✗ Failed to load metric exporter: datadog - Connection refused
```

### Configuration Not Found

SDK will print:
```
No configuration file found, using defaults
```

Ensure config is in one of the searched locations.

### Testing Configuration

Use console exporter to verify:

```yaml
exporters:
  metrics:
    - type: "console"
      enabled: true
```

You'll see metrics printed to stdout.

## Adding Custom Exporters

Implement `ExporterProvider`:

```java
public class CustomExporterProvider implements ExporterProvider {
    @Override
    public Exporter<?> createExporter(ExporterConfig config) {
        return new CustomExporter(config.getEndpoint());
    }
}
```

Register in `ExporterFactory`:

```java
factory.registerMetricProvider("custom", new CustomExporterProvider());
```

Now use in YAML:

```yaml
exporters:
  metrics:
    - type: "custom"
      endpoint: "http://custom-backend"
```

## Complete Example

See `config-examples/` directory for:
- `grafana-cloud.yml` - Grafana Cloud setup
- `datadog.yml` - Datadog configuration
- `prometheus-jaeger.yml` - Open-source stack
- `aws-cloudwatch.yml` - AWS native
- `multi-backend.yml` - Multiple backends

Run the example:

```bash
mvn exec:java -Dexec.mainClass="com.observability.examples.ConfigBasedExample"
```

---

**Zero Code Changes. Maximum Flexibility. Enterprise Ready.** 🚀
