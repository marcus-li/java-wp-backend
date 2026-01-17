package com.trading.backend.service;

import tools.jackson.databind.json.JsonMapper;
import com.trading.backend.config.SchwabProperties;
import com.trading.backend.model.TokenState;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenStorageService {

    private final SchwabProperties schwabProperties;
    private final JsonMapper mapper;

    /**
     * Platform-agnostic path resolver.
     * If the path in .env is "tokens/schwab.dat", this maps to [current-dir]/tokens/schwab.dat
     */
    private Path getResolvedPath() {
        return Path.of(System.getProperty("user.dir")).resolve(schwabProperties.token().storagePath());
    }

    @PostConstruct
    public void init() {
        try {
            // FIX: This creates all missing parent folders automatically
            Files.createDirectories(getResolvedPath().getParent());
            log.info("Token storage ready at: {}", getResolvedPath().toAbsolutePath());
        } catch (IOException e) {
            log.error("Could not initialize storage directories: {}", e.getMessage());
        }
    }

    public synchronized void saveToken(String accessToken, String refreshToken, int expiresInSec) {
        Path path = getResolvedPath();
        try {
            var state = new TokenState(
                    accessToken,
                    refreshToken,
                    Instant.now().plusSeconds(expiresInSec),
                    Instant.now().plus(7, ChronoUnit.DAYS)
            );

            // Jackson 3 writeValue accepts a File, Path, or Stream
            mapper.writeValue(path.toFile(), state);
            log.info("Successfully persisted Schwab token to {}", path.toAbsolutePath());

        } catch (Exception e) {
            log.error("CRITICAL: Failed to save Schwab token to disk at {}", path, e);
            throw new RuntimeException("Could not persist auth token", e);
        }
    }

    public synchronized Optional<TokenState> loadToken() {
        Path path = getResolvedPath();
        File file = path.toFile();

        if (!file.exists()) {
            log.debug("No token file found at {}", path.toAbsolutePath());
            return Optional.empty();
        }

        try {
            return Optional.of(mapper.readValue(file, TokenState.class));
        } catch (Exception e) {
            log.error("Failed to load or parse token file: {}", path.toAbsolutePath(), e);
            return Optional.empty();
        }
    }
}