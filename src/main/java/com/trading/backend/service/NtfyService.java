package com.trading.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class NtfyService {

    public static final String DEFAULT_TOPIC = "trading-app-backend";
    private final RestClient ntfyClient = RestClient.create();

    /**
     * Sends a notification to ntfy.sh if at least 6 hours have passed
     * since the previous successful notification.
     *
     * @param message The alert content to send to ntfy.sh
     */
    public void sendNtfyNotification(String message, String topic, AtomicLong lastNotificationTime, long rateLimitMillis) {
        long now = Instant.now().toEpochMilli();
        long previous = lastNotificationTime.get();

        if (now - previous <= rateLimitMillis) {
            return;
        }

        if (!lastNotificationTime.compareAndSet(previous, now)) {
            return;
        }

        try {
            ntfyClient.post()
                    .uri("https://ntfy.sh/" + topic)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(message)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to reach ntfy.sh: {}", e.getMessage());
            // rollback on failure
            lastNotificationTime.set(previous);
        }
    }

    public void sendNtfyNotification(String message, String topic) {
        try {
            ntfyClient.post()
                    .uri("https://ntfy.sh/" + topic)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(message)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Ntfy alert dispatched for token expiration.");
        } catch (Exception e) {
            log.error("Failed to reach ntfy.sh: {}", e.getMessage());
        }
    }

}
