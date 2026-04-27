package com.rubymusic.interaction.config;

import com.rubymusic.interaction.client.playlist.ApiClient;
import com.rubymusic.interaction.client.playlist.api.InternalPlaylistApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configures the OpenAPI-generated playlist service client.
 *
 * <p>Adds a Bearer token interceptor that injects the service JWT on every request.
 * The token is fetched (and cached with auto-refresh) by {@link ServiceTokenProvider}.
 *
 * <p>Uses a @LoadBalanced RestTemplate so Eureka resolves "playlist-service".
 *
 * <p>Timeouts: 2s connect / 5s read. Without these, a slow/hung playlist-service
 * blocks Tomcat threads indefinitely under concurrent load (cascading freeze).
 */
@Configuration
public class PlaylistServiceClientConfiguration {

    @Bean("playlistRestTemplate")
    @LoadBalanced
    public RestTemplate playlistRestTemplate(RestTemplateBuilder builder,
                                             ServiceTokenProvider serviceTokenProvider) {
        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().setBearerAuth(serviceTokenProvider.getToken());
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    @Bean
    public ApiClient playlistApiClient(
            @Qualifier("playlistRestTemplate") RestTemplate playlistRestTemplate,
            @Value("${services.playlist.base-url:http://playlist-service}") String baseUrl) {
        return new ApiClient(playlistRestTemplate).setBasePath(baseUrl);
    }

    @Bean
    public InternalPlaylistApi internalPlaylistApi(ApiClient playlistApiClient) {
        return new InternalPlaylistApi(playlistApiClient);
    }
}
