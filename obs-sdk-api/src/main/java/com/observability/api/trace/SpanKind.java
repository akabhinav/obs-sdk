package com.observability.api.trace;

/**
 * Enum representing the kind of span.
 */
public enum SpanKind {
    /**
     * Internal operation within the application.
     */
    INTERNAL,

    /**
     * Server-side handling of a synchronous RPC or HTTP request.
     */
    SERVER,

    /**
     * Client-side request.
     */
    CLIENT,

    /**
     * Producer sending a message to a broker.
     */
    PRODUCER,

    /**
     * Consumer receiving a message from a broker.
     */
    CONSUMER
}
