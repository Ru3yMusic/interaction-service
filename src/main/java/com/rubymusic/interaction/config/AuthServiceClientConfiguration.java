package com.rubymusic.interaction.config;

import com.rubymusic.interaction.client.auth.ApiClient;
import com.rubymusic.interaction.client.auth.api.InternalAuthApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configures the OpenAPI-generated auth service client (M2M token acquisition).
 *
 * <p>No Bearer token interceptor — this client IS the token acquisition mechanism.
 * X-Service-Name and X-Service-Secret are passed as explicit method arguments
 * (generated from header parameters in the spec).
 *
 * <p>Uses a @LoadBalanced RestTemplate so Eureka resolves "auth-service".
 */
@Configuration
public class AuthServiceClientConfiguration {

    @Bean("authRestTemplate")
    @LoadBalanced
    public RestTemplate authRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ApiClient authApiClient(
            @Qualifier("authRestTemplate") RestTemplate authRestTemplate,
            @Value("${services.auth.base-url:http://auth-service}") String baseUrl) {
        return new ApiClient(authRestTemplate).setBasePath(baseUrl);
    }

    @Bean
    public InternalAuthApi internalAuthApi(ApiClient authApiClient) {
        return new InternalAuthApi(authApiClient);
    }
}
