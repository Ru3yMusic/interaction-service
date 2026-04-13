package com.rubymusic.interaction.config;

import com.rubymusic.interaction.client.playlist.ApiClient;
import com.rubymusic.interaction.client.playlist.api.InternalPlaylistApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configures the OpenAPI-generated playlist service client.
 *
 * <p>Adds a Bearer token interceptor that injects the service JWT on every request.
 * The token is fetched (and cached with auto-refresh) by {@link ServiceTokenProvider}.
 *
 * <p>Uses a @LoadBalanced RestTemplate so Eureka resolves "playlist-service".
 */
@Configuration
public class PlaylistServiceClientConfiguration {

    @Bean("playlistRestTemplate")
    @LoadBalanced
    public RestTemplate playlistRestTemplate(ServiceTokenProvider serviceTokenProvider) {
        RestTemplate restTemplate = new RestTemplate();
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
