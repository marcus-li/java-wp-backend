package com.trading.backend.service

import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicLong

class NtfyServiceSpec extends Specification {

    def "sending two notifications within rate limit sends only once"() {
        given:
        def service = new NtfyService()

        def restClient = Mock(RestClient)
        def requestSpec = Mock(RestClient.RequestBodyUriSpec)
        def responseSpec = Mock(RestClient.ResponseSpec)

        // Inject mocked RestClient
        def field = NtfyService.getDeclaredField("ntfyClient")
        field.accessible = true
        field.set(service, restClient)

        def lastNotificationTime = new AtomicLong(0)
        long rateLimitMillis = 6 * 60 * 60 * 1000

        when:
        service.sendNtfyNotification(
                "test-message",
                NtfyService.DEFAULT_TOPIC,
                lastNotificationTime,
                rateLimitMillis
        )

        service.sendNtfyNotification(
                "test-message",
                NtfyService.DEFAULT_TOPIC,
                lastNotificationTime,
                rateLimitMillis
        )

        then:
        1 * restClient.post() >> requestSpec
        1 * requestSpec.uri("https://ntfy.sh/" + NtfyService.DEFAULT_TOPIC) >> requestSpec
        1 * requestSpec.contentType(MediaType.TEXT_PLAIN) >> requestSpec
        1 * requestSpec.body("test-message") >> requestSpec
        1 * requestSpec.retrieve() >> responseSpec
        1 * responseSpec.toBodilessEntity()
        0 * _
    }

    def "sending two notifications without rate limit sends only once"() {
        given:
        def service = new NtfyService()

        def restClient = Mock(RestClient)
        def requestSpec = Mock(RestClient.RequestBodyUriSpec)
        def responseSpec = Mock(RestClient.ResponseSpec)

        def field = NtfyService.getDeclaredField("ntfyClient")
        field.accessible = true
        field.set(service, restClient)

        when:
        service.sendNtfyNotification(
                "test-message",
                NtfyService.DEFAULT_TOPIC
        )

        service.sendNtfyNotification(
                "test-message",
                NtfyService.DEFAULT_TOPIC
        )

        then:
        2 * restClient.post() >> requestSpec
        2 * requestSpec.uri("https://ntfy.sh/" + NtfyService.DEFAULT_TOPIC) >> requestSpec
        2 * requestSpec.contentType(MediaType.TEXT_PLAIN) >> requestSpec
        2 * requestSpec.body("test-message") >> requestSpec
        2 * requestSpec.retrieve() >> responseSpec
        2 * responseSpec.toBodilessEntity()
        0 * _
    }

}