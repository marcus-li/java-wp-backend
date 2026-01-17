package com.trading.backend.model;

import java.time.Instant;

// Internal wrapper to track local expiration time
public record TokenState(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
    public boolean isAccessTokenExpired() {
        return Instant.now().isAfter(accessTokenExpiresAt.minusSeconds(30)); // 30s buffer
    }

    public boolean isRefreshTokenExpired() {
        return Instant.now().isAfter(refreshTokenExpiresAt);
    }
}