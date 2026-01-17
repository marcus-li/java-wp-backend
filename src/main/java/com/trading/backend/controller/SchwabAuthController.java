package com.trading.backend.controller;

import com.trading.backend.config.SchwabProperties;
import com.trading.backend.service.SchwabAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SchwabAuthController {

    private final SchwabAuthService authService;
    private final SchwabProperties props;

    /**
     * Entry Point: Redirects the user to Schwab login
     */
    @GetMapping("/login-url")
    public RedirectView redirectToSchwab() {
        String url = String.format(
                "https://api.schwabapi.com/v1/oauth/authorize?client_id=%s&redirect_uri=%s",
                props.api().clientId().trim(),
                props.api().redirectUri().trim()
        );
        log.info("Redirecting to Schwab: {}", url);
        return new RedirectView(url);
    }

    /**
     * Callback Point: Receives code at https://127.0.0.1/
     */
    @GetMapping("/")
    public String handleSchwabRedirect(@RequestParam(value = "code", required = false) String code) {
        if (code == null) {
            return "<h1>Ready</h1><p><a href='/login-url'>Login with Schwab</a></p>";
        }

        log.info("Auth code received. Swapping...");
        try {
            authService.exchangeAuthCode(code);
            return """
                   <div style='font-family:sans-serif; text-align:center; padding:50px;'>
                       <h1 style='color:#2e7d32;'>Success!</h1>
                       <p>App authorized. You can close this window.</p>
                   </div>
                   """;
        } catch (Exception e) {
            log.error("Exchange failed: {}", e.getMessage());
            return "<h1>Auth Failed</h1><p>Check logs. Error: " + e.getMessage() + "</p>";
        }
    }
}