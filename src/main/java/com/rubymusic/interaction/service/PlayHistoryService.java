package com.rubymusic.interaction.service;

import com.rubymusic.interaction.model.PlayHistory;
import com.rubymusic.interaction.model.UserPlaybackCheckpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PlayHistoryService {

    /**
     * Recordss a play and publishes a {@code song.played} Kafka event so
     * catalog-service can increment the song's play_count asynchronously.
     */
    void recordPlay(UUID userId, UUID songId, int durationPlayedSeconds);

    Page<PlayHistory> getPlayHistory(UUID userId, Pageable pageable);

    void savePlaybackCheckpoint(UUID userId, UUID songId, int currentTimeSeconds);

    Optional<UserPlaybackCheckpoint> getPlaybackCheckpoint(UUID userId);
}
