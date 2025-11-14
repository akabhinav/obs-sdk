package com.observability.api.exporter;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Base interface for exporters.
 * Exporters send observability data to backends (e.g., OpenTelemetry, Prometheus).
 *
 * @param <T> the type of data to export
 */
public interface Exporter<T> extends AutoCloseable {

    /**
     * Exports a batch of data.
     *
     * @param data the data to export
     * @return result indicating success or failure
     */
    ExportResult export(Collection<T> data);

    /**
     * Asynchronously exports a batch of data.
     *
     * @param data the data to export
     * @return a future that completes with the export result
     */
    default CompletableFuture<ExportResult> exportAsync(Collection<T> data) {
        return CompletableFuture.supplyAsync(() -> export(data));
    }

    /**
     * Flushes any pending data.
     *
     * @return result indicating success or failure
     */
    ExportResult flush();

    /**
     * Shuts down the exporter.
     */
    void shutdown();

    /**
     * Closes the exporter (calls shutdown).
     */
    @Override
    default void close() {
        shutdown();
    }

    /**
     * Result of an export operation.
     */
    enum ExportResult {
        /**
         * Export succeeded.
         */
        SUCCESS,

        /**
         * Export failed but can be retried.
         */
        FAILURE_RETRYABLE,

        /**
         * Export failed and should not be retried.
         */
        FAILURE_NOT_RETRYABLE
    }
}
