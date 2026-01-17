package com.trading.backend.config;

import com.trading.backend.service.SchwabAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class SchwabClientConfig {

    private final SchwabAuthService authService;
    private final SchwabProperties props;

    @Bean("schwabRestClient")
    public RestClient schwabRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(props.api().baseUrl() + "/trader/v1") // Append trader version
                .requestInterceptor((request, body, execution) -> {
                    // Auto-inject the token on every request
                    String token = authService.getValidAccessToken();
                    request.getHeaders().setBearerAuth(token);
                    return execution.execute(request, body);
                })
                .build();
    }
}