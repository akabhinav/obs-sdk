package com.observability.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads observability configuration from YAML or JSON files.
 * Supports multiple configuration sources with precedence.
 */
public class ConfigLoader {

    private static final String DEFAULT_CONFIG_FILE = "observability.yml";
    private static final String[] CONFIG_LOCATIONS = {
        "observability.yml",
        "observability.yaml",
        "config/observability.yml",
        "config/observability.yaml",
        System.getProperty("user.home") + "/.observability/config.yml"
    };

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public ConfigLoader() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }

    /**
     * Loads configuration from default locations.
     * Searches in order: current dir, config/ dir, home dir.
     */
    public ObservabilityConfig loadConfig() {
        // Try environment variable first
        String configPath = System.getenv("OBSERVABILITY_CONFIG");
        if (configPath != null && !configPath.isEmpty()) {
            try {
                return loadConfigFromFile(Paths.get(configPath));
            } catch (IOException e) {
                System.err.println("Failed to load config from OBSERVABILITY_CONFIG: " + e.getMessage());
            }
        }

        // Try system property
        configPath = System.getProperty("observability.config");
        if (configPath != null && !configPath.isEmpty()) {
            try {
                return loadConfigFromFile(Paths.get(configPath));
            } catch (IOException e) {
                System.err.println("Failed to load config from system property: " + e.getMessage());
            }
        }

        // Try default locations
        for (String location : CONFIG_LOCATIONS) {
            Path path = Paths.get(location);
            if (Files.exists(path)) {
                try {
                    return loadConfigFromFile(path);
                } catch (IOException e) {
                    System.err.println("Failed to load config from " + location + ": " + e.getMessage());
                }
            }
        }

        // Try classpath
        try {
            return loadConfigFromClasspath(DEFAULT_CONFIG_FILE);
        } catch (IOException e) {
            // Ignore, will use defaults
        }

        // Return default configuration
        System.out.println("No configuration file found, using defaults");
        return new ObservabilityConfig();
    }

    /**
     * Loads configuration from a specific file.
     */
    public ObservabilityConfig loadConfigFromFile(Path path) throws IOException {
        String fileName = path.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
            return yamlMapper.readValue(path.toFile(), ObservabilityConfig.class);
        } else if (fileName.endsWith(".json")) {
            return jsonMapper.readValue(path.toFile(), ObservabilityConfig.class);
        } else {
            throw new IOException("Unsupported config file format: " + fileName);
        }
    }

    /**
     * Loads configuration from classpath.
     */
    public ObservabilityConfig loadConfigFromClasspath(String resourcePath) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IOException("Config file not found in classpath: " + resourcePath);
        }

        if (resourcePath.endsWith(".yml") || resourcePath.endsWith(".yaml")) {
            return yamlMapper.readValue(stream, ObservabilityConfig.class);
        } else if (resourcePath.endsWith(".json")) {
            return jsonMapper.readValue(stream, ObservabilityConfig.class);
        } else {
            throw new IOException("Unsupported config file format: " + resourcePath);
        }
    }

    /**
     * Loads configuration from a string.
     */
    public ObservabilityConfig loadConfigFromString(String configContent, boolean isYaml) throws IOException {
        if (isYaml) {
            return yamlMapper.readValue(configContent, ObservabilityConfig.class);
        } else {
            return jsonMapper.readValue(configContent, ObservabilityConfig.class);
        }
    }
}
