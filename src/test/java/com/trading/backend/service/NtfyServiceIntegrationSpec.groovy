package com.trading.backend.service

import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestClient
import spock.lang.Specification
import java.util.concurrent.atomic.AtomicLong

class NtfyServiceIntegrationSpec extends Specification {

    NtfyService ntfyService

    // Use the real ntfy URL
    String liveNtfyUrl = "https://ntfy.sh"

    def setup() {
        // 1. Manually instantiate
        ntfyService = new NtfyService()

        // 2. Create a REAL RestClient pointing to the live internet
        def realClient = RestClient.builder()
                .baseUrl(liveNtfyUrl)
                .build()

        // 3. Inject it
        ReflectionTestUtils.setField(ntfyService, "ntfyClient", realClient)
    }

    def "actually sends a notification to ntfy.sh"() {
        given: "A timestamp for rate limiting that allows immediate sending"
        def lastNotificationTime = new AtomicLong(0)
        long rateLimitMillis = 1000

        when: "We call the service"
        ntfyService.sendNtfyNotification(
                "Java 25 Live Test: The trading bot is online!",
                NtfyService.DEFAULT_TOPIC,
                lastNotificationTime,
                rateLimitMillis
        )

        then: "The call completes without exception"
        // Since it's a real network call, we just verify no crash occurred
        noExceptionThrown()

        println "Check your ntfy app for topic: ${ NtfyService.DEFAULT_TOPIC}"
    }
}