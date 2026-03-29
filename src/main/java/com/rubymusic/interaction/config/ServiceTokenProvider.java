package com.rubymusic.interaction.config;

import com.rubymusic.interaction.client.AuthServiceClient;
import com.rubymusic.interaction.dto.ServiceTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Fetches and caches a service JWT from auth-service (zero-trust M2M).
 * Refreshes automatically 1 minute before expiry.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceTokenProvider {

    private final AuthServiceClient authServiceClient;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${internal.service-secret}")
    private String serviceSecret;

    private volatile String cachedToken;
    private volatile long tokenExpiresAt;

    public String getToken() {
        if (cachedToken == null || System.currentTimeMillis() > tokenExpiresAt - 60_000) {
            ServiceTokenResponse response = authServiceClient.getServiceToken(serviceName, serviceSecret);
            cachedToken = response.token();
            tokenExpiresAt = System.currentTimeMillis() + (response.expiresIn() * 1000L);
            log.debug("Refreshed service token for: {}", serviceName);
        }
        return cachedToken;
    }
}
