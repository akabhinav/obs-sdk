package com.observability.core.exporter;

import com.observability.api.exporter.Exporter;
import com.observability.core.config.ObservabilityConfig.ExporterConfig;

/**
 * Service provider interface for creating exporters.
 * Allows plugins to register custom exporters.
 */
public interface ExporterProvider {

    /**
     * Creates an exporter from the given configuration.
     *
     * @param config the exporter configuration
     * @return the created exporter
     */
    Exporter<?> createExporter(ExporterConfig config);

    /**
     * Returns the type identifier for this exporter.
     */
    default String getType() {
        return getClass().getSimpleName()
            .replace("ExporterProvider", "")
            .toLowerCase();
    }
}
