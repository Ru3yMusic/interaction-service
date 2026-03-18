package com.rubymusic.interaction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SongInteractionService {

    /** Likes a song and publishes a Kafka event to update catalog likes_count */
    void likeSong(UUID userId, UUID songId);

    void unlikeSong(UUID userId, UUID songId);

    boolean isLiked(UUID userId, UUID songId);

    /** Returns liked song IDs (paginated) */
    Page<UUID> getLikedSongs(UUID userId, Pageable pageable);

    /** Hides a song within a specific album — does not affect other albums */
    void hideSong(UUID userId, UUID songId, UUID albumId);

    void unhideSong(UUID userId, UUID songId, UUID albumId);

    List<UUID> getHiddenSongsByAlbum(UUID userId, UUID albumId);
}
