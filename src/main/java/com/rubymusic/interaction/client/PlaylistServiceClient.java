package com.rubymusic.interaction.client;

import com.rubymusic.interaction.config.ServiceAuthFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

/**
 * Feign client for playlist-service internal endpoints.
 * Keeps the system playlist ("Tus me gusta") in sync with song likes/unlikes.
 */
@FeignClient(name = "playlist-service", configuration = ServiceAuthFeignConfig.class)
public interface PlaylistServiceClient {

    @PostMapping("/api/v1/playlists/internal/system/{userId}/songs/{songId}")
    void addSongToSystemPlaylist(@PathVariable("userId") UUID userId,
                                 @PathVariable("songId") UUID songId);

    @DeleteMapping("/api/v1/playlists/internal/system/{userId}/songs/{songId}")
    void removeSongFromSystemPlaylist(@PathVariable("userId") UUID userId,
                                      @PathVariable("songId") UUID songId);
}
