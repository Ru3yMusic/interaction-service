package com.rubymusic.interaction.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

/**
 * Per-client Feign configuration that injects the service JWT into every outgoing request.
 * Registered only for PlaylistServiceClient — NOT for AuthServiceClient.
 *
 * Not annotated with @Configuration — registered only for specific Feign clients.
 */
@RequiredArgsConstructor
public class ServiceAuthFeignConfig {

    private final ServiceTokenProvider serviceTokenProvider;

    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return template -> template.header("Authorization", "Bearer " + serviceTokenProvider.getToken());
    }
}
