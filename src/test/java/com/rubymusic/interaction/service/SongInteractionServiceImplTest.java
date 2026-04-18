package com.rubymusic.interaction.service;

import com.rubymusic.interaction.client.playlist.api.InternalPlaylistApi;
import com.rubymusic.interaction.client.playlist.model.SystemSongRequest;
import com.rubymusic.interaction.model.SongLike;
import com.rubymusic.interaction.model.id.SongLikeId;
import com.rubymusic.interaction.repository.HiddenSongRepository;
import com.rubymusic.interaction.repository.SongLikeRepository;
import com.rubymusic.interaction.service.impl.SongInteractionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SongInteractionServiceImpl} — playlist sync via generated client.
 *
 * TDD cycle:
 * RED  — @InjectMocks injects InternalPlaylistApi but production code still has
 *         PlaylistServiceClient field → mock never injected → verify fails
 * GREEN — SongInteractionServiceImpl refactored to use InternalPlaylistApi → tests pass
 * TRIANGULATE — covers: like syncs add, unlike syncs remove, client failure is swallowed
 */
@ExtendWith(MockitoExtension.class)
class SongInteractionServiceImplTest {

    @Mock private SongLikeRepository songLikeRepository;
    @Mock private HiddenSongRepository hiddenSongRepository;
    @Mock @SuppressWarnings("rawtypes") private KafkaTemplate kafkaTemplate;
    @Mock private InternalPlaylistApi internalPlaylistApi;

    @InjectMocks private SongInteractionServiceImpl service;

    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SONG_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    // ── likeSong ──────────────────────────────────────────────────────────────

    @Test
    void likeSong_newLike_callsAddSongToSystemPlaylist() {
        // Given — song not yet liked, save succeeds, kafka doesn't matter
        when(songLikeRepository.existsByIdUserIdAndIdSongId(USER_ID, SONG_ID)).thenReturn(false);
        when(songLikeRepository.save(any(SongLike.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        service.likeSong(USER_ID, SONG_ID);

        // Then — playlist client must be called to keep system playlist in sync
        verify(internalPlaylistApi, times(1)).addSongToSystemPlaylistInternal(argThat(req ->
                USER_ID.equals(req.getUserId()) && SONG_ID.equals(req.getSongId())));
    }

    // TRIANGULATE: playlist client failure must NOT propagate (fire-and-forget)
    @Test
    void likeSong_playlistClientThrows_doesNotPropagate() {
        // Given
        when(songLikeRepository.existsByIdUserIdAndIdSongId(USER_ID, SONG_ID)).thenReturn(false);
        when(songLikeRepository.save(any(SongLike.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RestClientException("playlist-service unavailable"))
                .when(internalPlaylistApi).addSongToSystemPlaylistInternal(any(SystemSongRequest.class));

        // When / Then — exception is swallowed; no exception escapes the service
        assertThatNoException().isThrownBy(() -> service.likeSong(USER_ID, SONG_ID));
    }

    // TRIANGULATE: idempotent — duplicate like skips playlist sync entirely
    @Test
    void likeSong_alreadyLiked_skipsPlaylistSync() {
        // Given — song already liked
        when(songLikeRepository.existsByIdUserIdAndIdSongId(USER_ID, SONG_ID)).thenReturn(true);

        // When
        service.likeSong(USER_ID, SONG_ID);

        // Then — playlist client must NOT be called (idempotency guard)
        verify(internalPlaylistApi, never()).addSongToSystemPlaylistInternal(any(SystemSongRequest.class));
    }

    // ── unlikeSong ────────────────────────────────────────────────────────────

    @Test
    void unlikeSong_existingLike_callsRemoveSongFromSystemPlaylist() {
        // Given
        SongLikeId likeId = new SongLikeId(USER_ID, SONG_ID);
        when(songLikeRepository.existsById(likeId)).thenReturn(true);

        // When
        service.unlikeSong(USER_ID, SONG_ID);

        // Then
        verify(internalPlaylistApi, times(1)).removeSongFromSystemPlaylistInternal(USER_ID, SONG_ID);
    }

    // TRIANGULATE: playlist client failure on unlike must also be swallowed
    @Test
    void unlikeSong_playlistClientThrows_doesNotPropagate() {
        // Given
        SongLikeId likeId = new SongLikeId(USER_ID, SONG_ID);
        when(songLikeRepository.existsById(likeId)).thenReturn(true);
        doThrow(new RestClientException("playlist-service unavailable"))
                .when(internalPlaylistApi).removeSongFromSystemPlaylistInternal(USER_ID, SONG_ID);

        // When / Then
        assertThatNoException().isThrownBy(() -> service.unlikeSong(USER_ID, SONG_ID));
    }
}
