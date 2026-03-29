package com.rubymusic.interaction.client;

import com.rubymusic.interaction.dto.ServiceTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client to request a service JWT from auth-service (zero-trust M2M).
 * No auth interceptor attached — this IS the token acquisition call.
 */
@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @PostMapping("/api/v1/auth/internal/service-token")
    ServiceTokenResponse getServiceToken(
            @RequestHeader("X-Service-Name") String serviceName,
            @RequestHeader("X-Service-Secret") String serviceSecret);
}
