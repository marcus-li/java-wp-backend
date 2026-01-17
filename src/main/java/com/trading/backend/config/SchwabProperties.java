package com.trading.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * A Java record that provides type-safe configuration properties for Schwab API settings.
 *
 * This class is designed to be used with Spring Boot's {@link ConfigurationProperties}
 * mechanism, binding values from application configuration files (like application.properties
 * or application.yml) that start with the prefix {@code "schwab"}.
 *
 * It models nested properties using nested record types for a clean, immutable, and
 * hierarchical configuration structure.
 *
 * To activate this configuration binding in a Spring Boot application, you must either:
 * 1. Add {@code @EnableConfigurationProperties(SchwabProperties.class)} to a main
 *    configuration class.
 * 2. Or, annotate this record itself with a stereotype annotation like {@code @Component}
 *    to enable component scanning.
 *
 * Example properties file structure:
 * {@code
 * schwab.api.base-url=...
 * schwab.api.client-id=...
 * schwab.token.storage-path=...
 * }
 *
 * @param api    Configuration group for API connection details (base URL, client ID/secret).
 * @param token  Configuration group for token management specifics (storage path).
 */
@ConfigurationProperties(prefix = "schwab")
public record SchwabProperties(Api api, Token token) {

    /**
     * Represents the API connection details within the main {@code SchwabProperties} group.
     *
     * @param baseUrl      The base URL endpoint for the Schwab API (e.g., https://api.schwabapi.com).
     * @param clientId     The application's client ID for OAuth 2.0 authentication.
     * @param clientSecret The application's client secret for OAuth 2.0 authentication.
     */
    public record Api(String baseUrl, String clientId, String clientSecret,
                      String redirectUri
                      ) {}

    /**
     * Represents configuration related to where authentication tokens are stored.
     *
     * @param storagePath The file system path where refresh/access tokens should be saved and loaded from.
     */
    public record Token(String storagePath) {}
}