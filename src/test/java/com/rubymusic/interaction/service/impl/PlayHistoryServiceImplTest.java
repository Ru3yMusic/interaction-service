package com.rubymusic.interaction.service.impl;

import com.rubymusic.interaction.model.PlayHistory;
import com.rubymusic.interaction.model.UserPlaybackCheckpoint;
import com.rubymusic.interaction.repository.PlayHistoryRepository;
import com.rubymusic.interaction.repository.UserPlaybackCheckpointRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayHistoryServiceImplTest {

    @Mock
    private PlayHistoryRepository playHistoryRepository;

    @Mock
    private UserPlaybackCheckpointRepository userPlaybackCheckpointRepository;

    @Mock
    @SuppressWarnings("rawtypes")
    private KafkaTemplate kafkaTemplate;

    @InjectMocks
    private PlayHistoryServiceImpl service;

    private static final UUID USER = UUID.randomUUID();
    private static final UUID SONG = UUID.randomUUID();

    // ── recordPlay ────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void recordPlay_persists_andEmitsKafkaEvent() {
        service.recordPlay(USER, SONG, 30);

        verify(playHistoryRepository).save(any(PlayHistory.class));
        verify(kafkaTemplate).send("song.played", SONG.toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void recordPlay_persistsCorrectFields() {
        ArgumentCaptor<PlayHistory> captor = ArgumentCaptor.forClass(PlayHistory.class);

        service.recordPlay(USER, SONG, 45);

        verify(playHistoryRepository).save(captor.capture());
        PlayHistory saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER);
        assertThat(saved.getSongId()).isEqualTo(SONG);
        assertThat(saved.getDurationPlayed()).isEqualTo(45);
    }

    // ── getPlayHistory ────────────────────────────────────────────────────────

    @Test
    void getPlayHistory_delegatesToRepository() {
        Pageable pageable = Pageable.ofSize(10);
        when(playHistoryRepository.findAllByUserIdOrderByPlayedAtDesc(USER, pageable))
                .thenReturn(Page.empty());

        service.getPlayHistory(USER, pageable);

        verify(playHistoryRepository).findAllByUserIdOrderByPlayedAtDesc(USER, pageable);
    }

    // ── savePlaybackCheckpoint ────────────────────────────────────────────────

    @Test
    void savePlaybackCheckpoint_existing_updatesIt() {
        UserPlaybackCheckpoint existing = UserPlaybackCheckpoint.builder()
                .userId(USER)
                .songId(UUID.randomUUID())
                .currentTimeSeconds(10)
                .build();
        when(userPlaybackCheckpointRepository.findById(USER)).thenReturn(Optional.of(existing));

        service.savePlaybackCheckpoint(USER, SONG, 60);

        ArgumentCaptor<UserPlaybackCheckpoint> captor = ArgumentCaptor.forClass(UserPlaybackCheckpoint.class);
        verify(userPlaybackCheckpointRepository).save(captor.capture());
        UserPlaybackCheckpoint saved = captor.getValue();
        assertThat(saved.getSongId()).isEqualTo(SONG);
        assertThat(saved.getCurrentTimeSeconds()).isEqualTo(60);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void savePlaybackCheckpoint_notFound_createsNew() {
        when(userPlaybackCheckpointRepository.findById(USER)).thenReturn(Optional.empty());

        service.savePlaybackCheckpoint(USER, SONG, 30);

        ArgumentCaptor<UserPlaybackCheckpoint> captor = ArgumentCaptor.forClass(UserPlaybackCheckpoint.class);
        verify(userPlaybackCheckpointRepository).save(captor.capture());
        UserPlaybackCheckpoint saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER);
        assertThat(saved.getSongId()).isEqualTo(SONG);
        assertThat(saved.getCurrentTimeSeconds()).isEqualTo(30);
    }

    @Test
    void savePlaybackCheckpoint_negativeCurrentTime_clampsToZero() {
        when(userPlaybackCheckpointRepository.findById(USER)).thenReturn(Optional.empty());

        service.savePlaybackCheckpoint(USER, SONG, -50);

        ArgumentCaptor<UserPlaybackCheckpoint> captor = ArgumentCaptor.forClass(UserPlaybackCheckpoint.class);
        verify(userPlaybackCheckpointRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrentTimeSeconds()).isZero();
    }

    // ── getPlaybackCheckpoint ─────────────────────────────────────────────────

    @Test
    void getPlaybackCheckpoint_existing_returnsIt() {
        UserPlaybackCheckpoint existing = UserPlaybackCheckpoint.builder().userId(USER).build();
        when(userPlaybackCheckpointRepository.findById(USER)).thenReturn(Optional.of(existing));

        Optional<UserPlaybackCheckpoint> result = service.getPlaybackCheckpoint(USER);

        assertThat(result).contains(existing);
    }

    @Test
    void getPlaybackCheckpoint_absent_returnsEmpty() {
        when(userPlaybackCheckpointRepository.findById(USER)).thenReturn(Optional.empty());

        assertThat(service.getPlaybackCheckpoint(USER)).isEmpty();
    }
}
