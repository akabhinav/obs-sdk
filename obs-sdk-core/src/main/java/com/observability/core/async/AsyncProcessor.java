package com.observability.core.async;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Async processor using Java 21 virtual threads for high-throughput operations.
 * Handles batching and async processing of observability data.
 */
public final class AsyncProcessor<T> {

    private final int batchSize;
    private final long batchTimeoutMs;
    private final Consumer<Collection<T>> processor;
    private final ExecutorService executor;
    private final BlockingQueue<T> queue;
    private final ScheduledExecutorService scheduler;

    private volatile boolean running = true;

    public AsyncProcessor(
        int batchSize,
        long batchTimeoutMs,
        Consumer<Collection<T>> processor
    ) {
        this.batchSize = batchSize;
        this.batchTimeoutMs = batchTimeoutMs;
        this.processor = processor;
        this.queue = new LinkedBlockingQueue<>();

        // Use virtual threads for processing
        this.executor = Executors.newVirtualThreadPerTaskExecutor();

        // Use platform thread for scheduled tasks
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("async-processor-scheduler");
            return thread;
        });

        startProcessing();
    }

    /**
     * Submits an item for async processing.
     */
    public void submit(T item) {
        if (!running) {
            throw new IllegalStateException("Processor is shutdown");
        }
        queue.offer(item);
    }

    /**
     * Submits multiple items for async processing.
     */
    public void submitAll(Collection<T> items) {
        if (!running) {
            throw new IllegalStateException("Processor is shutdown");
        }
        queue.addAll(items);
    }

    private void startProcessing() {
        // Start batch processing task
        scheduler.scheduleAtFixedRate(
            this::processBatch,
            batchTimeoutMs,
            batchTimeoutMs,
            TimeUnit.MILLISECONDS
        );
    }

    private void processBatch() {
        if (queue.isEmpty()) {
            return;
        }

        List<T> batch = new ArrayList<>();
        queue.drainTo(batch, batchSize);

        if (!batch.isEmpty()) {
            // Process batch using virtual thread
            executor.submit(() -> {
                try {
                    processor.accept(batch);
                } catch (Exception e) {
                    System.err.println("Error processing batch: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Flushes all pending items.
     */
    public CompletableFuture<Void> flush() {
        return CompletableFuture.runAsync(() -> {
            List<T> remaining = new ArrayList<>();
            queue.drainTo(remaining);

            if (!remaining.isEmpty()) {
                try {
                    processor.accept(remaining);
                } catch (Exception e) {
                    System.err.println("Error flushing: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, executor);
    }

    /**
     * Shuts down the processor.
     */
    public void shutdown() {
        running = false;

        // Flush remaining items
        flush().join();

        // Shutdown executors
        scheduler.shutdown();
        executor.shutdown();

        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns the current queue size.
     */
    public int queueSize() {
        return queue.size();
    }
}
