package com.trading.backend.service;

import com.trading.backend.config.SchwabProperties;
import com.trading.backend.model.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchwabAuthService {

    private final RestClient.Builder clientBuilder;
    private final TokenStorageService tokenStorage;
    private final SchwabProperties props;
    long sixHoursInMillis = Duration.ofHours(6).toMillis();
    // Track the last notification in milliseconds (default to 0)
    private final AtomicLong lastNotificationTime = new AtomicLong(0);
    private final NtfyService ntfyService;

    public void exchangeAuthCode(String authCode) {
        log.info("Swapping Auth Code for Tokens...");

        // FIX 1: Decode the code (converts %40 back to @)
        String decodedCode = URLDecoder.decode(authCode, StandardCharsets.UTF_8);

        var map = new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "authorization_code");
        map.add("code", decodedCode);
        map.add("redirect_uri", props.api().redirectUri());

        // Basic Auth Header (Ensure no spaces/newlines)
        String creds = props.api().clientId().trim() + ":" + props.api().clientSecret().trim();
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(creds.getBytes());

        // FIX 2: Use RestClient.create() to bypass any global interceptors
        // that might be calling getValidAccessToken() prematurely
        var cleanClient = RestClient.create(props.api().baseUrl());

        TokenResponse response = cleanClient.post()
                .uri("/v1/oauth/token")
                .header("Authorization", basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(map)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null) throw new RuntimeException("Initial Auth Exchange Failed");

        tokenStorage.saveToken(
                response.accessToken(),
                response.refreshToken(),
                response.expiresInSeconds()
        );
        log.info("SUCCESS: Tokens acquired and stored.");
    }


    /**
     * Returns a valid access token, refreshing it if necessary.
     */
    public String getValidAccessToken() {
        var tokenState = tokenStorage.loadToken()
                .orElseThrow(() -> new IllegalStateException("No token found. Inject Refresh Token via /api/admin/update-token"));

        if (tokenState.isRefreshTokenExpired()) {
            ntfyService.sendNtfyNotification("Schwab Refresh Token Expired! Manual re-auth required.", NtfyService.DEFAULT_TOPIC, lastNotificationTime, sixHoursInMillis);
            throw new IllegalStateException("FATAL: Refresh Token expired (7-day limit). Manual re-auth required.");
        }

        if (tokenState.isAccessTokenExpired()) {
            log.info("Access token expired. Refreshing...");
            return refreshAccessToken(tokenState.refreshToken());
        }

        return tokenState.accessToken();
    }

    public void manualUpdateRefreshToken(String newRefreshToken) {
        log.info("Manual refresh token update initiated.");
        refreshAccessToken(newRefreshToken);
    }

    private String refreshAccessToken(String refreshToken) {
        var map = new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "refresh_token");
        map.add("refresh_token", refreshToken);

        // Build Basic Auth Header on the fly or cache it
        String creds = props.api().clientId() + ":" + props.api().clientSecret();
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));

        // Create a temporary client for the auth call specifically
        var authClient = clientBuilder.baseUrl(props.api().baseUrl()).build();

        TokenResponse response = authClient.post()
                .uri("/v1/oauth/token")
                .header("Authorization", basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(map)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null) throw new RuntimeException("Schwab auth failed");

        String nextRefreshToken = StringUtils.hasText(response.refreshToken())
                ? response.refreshToken()
                : refreshToken;

        if (!Objects.equals(refreshToken, nextRefreshToken)) {
            log.info("Schwab Refresh Token has rotated. Updating persistent storage.");
            log.debug("Old: {}... New: {}...", refreshToken.substring(0, 8), nextRefreshToken.substring(0, 8));
        }

        // 3. Persist the state (Access token is always new, so we always save)
        tokenStorage.saveToken(
                response.accessToken(),
                nextRefreshToken,
                response.expiresInSeconds()
        );

        return response.accessToken();
    }



}