package com.observability.exporters.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for HTTP-based exporters.
 * Provides common HTTP client functionality.
 */
public abstract class HttpExporter implements AutoCloseable {

    protected final String endpoint;
    protected final Map<String, String> headers;
    protected final int timeoutMs;
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    protected volatile boolean shutdown = false;

    protected HttpExporter(String endpoint, Map<String, String> headers, int timeoutMs) {
        this.endpoint = endpoint;
        this.headers = new HashMap<>(headers);
        this.timeoutMs = timeoutMs;
        this.objectMapper = new ObjectMapper();

        // Create HTTP client with virtual thread executor
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofMillis(timeoutMs))
            .build();
    }

    /**
     * Sends an HTTP POST request with JSON payload.
     */
    protected HttpResponse<String> sendJson(Object payload) throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(payload);
        return sendRequest(jsonPayload, "application/json");
    }

    /**
     * Sends an HTTP POST request with custom content type.
     */
    protected HttpResponse<String> sendRequest(String body, String contentType) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .timeout(Duration.ofMillis(timeoutMs))
            .header("Content-Type", contentType)
            .POST(HttpRequest.BodyPublishers.ofString(body));

        // Add custom headers
        headers.forEach(requestBuilder::header);

        HttpRequest request = requestBuilder.build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Checks if the HTTP response indicates success.
     */
    protected boolean isSuccessful(HttpResponse<?> response) {
        int status = response.statusCode();
        return status >= 200 && status < 300;
    }

    /**
     * Checks if the HTTP response indicates a retryable error.
     */
    protected boolean isRetryable(HttpResponse<?> response) {
        int status = response.statusCode();
        // 429 (Too Many Requests), 500-599 (Server Errors) are retryable
        return status == 429 || (status >= 500 && status < 600);
    }

    @Override
    public void close() {
        shutdown = true;
    }
}
