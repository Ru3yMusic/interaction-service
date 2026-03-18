package com.rubymusic.interaction.service.impl;

import com.rubymusic.interaction.model.PlayHistory;
import com.rubymusic.interaction.repository.PlayHistoryRepository;
import com.rubymusic.interaction.service.PlayHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayHistoryServiceImpl implements PlayHistoryService {

    private static final String TOPIC_SONG_PLAYED = "song.played";

    private final PlayHistoryRepository playHistoryRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    public void recordPlay(UUID userId, UUID songId, int durationPlayedSeconds) {
        PlayHistory entry = PlayHistory.builder()
                .userId(userId)
                .songId(songId)
                .durationPlayed(durationPlayedSeconds)
                .build();
        playHistoryRepository.save(entry);

        // Notify catalog-service asynchronously — eventual consistency is acceptable for play_count
        kafkaTemplate.send(TOPIC_SONG_PLAYED, songId.toString());
        log.debug("Play recorded: user={} song={} duration={}s", userId, songId, durationPlayedSeconds);
    }

    @Override
    public Page<PlayHistory> getPlayHistory(UUID userId, Pageable pageable) {
        return playHistoryRepository.findAllByUserIdOrderByPlayedAtDesc(userId, pageable);
    }
}
