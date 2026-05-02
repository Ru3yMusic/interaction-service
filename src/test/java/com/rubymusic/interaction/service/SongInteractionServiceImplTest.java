package com.rubymusic.interaction.service;

import com.rubymusic.interaction.client.playlist.api.InternalPlaylistApi;
import com.rubymusic.interaction.client.playlist.model.SystemSongRequest;
import com.rubymusic.interaction.model.HiddenSong;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void unlikeSong_notLiked_skipsEverything() {
        SongLikeId likeId = new SongLikeId(USER_ID, SONG_ID);
        when(songLikeRepository.existsById(likeId)).thenReturn(false);

        service.unlikeSong(USER_ID, SONG_ID);

        verify(songLikeRepository, never()).deleteById(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString());
        verify(internalPlaylistApi, never()).removeSongFromSystemPlaylistInternal(any(), any());
    }

    @Test
    void likeSong_newLike_emitsKafkaEvent() {
        when(songLikeRepository.existsByIdUserIdAndIdSongId(USER_ID, SONG_ID)).thenReturn(false);
        when(songLikeRepository.save(any(SongLike.class))).thenAnswer(inv -> inv.getArgument(0));

        service.likeSong(USER_ID, SONG_ID);

        verify(kafkaTemplate).send("song.liked", SONG_ID.toString());
    }

    // ── isLiked ───────────────────────────────────────────────────────────────

    @Test
    void isLiked_present_returnsTrue() {
        when(songLikeRepository.existsByIdUserIdAndIdSongId(USER_ID, SONG_ID)).thenReturn(true);
        assertThat(service.isLiked(USER_ID, SONG_ID)).isTrue();
    }

    @Test
    void isLiked_absent_returnsFalse() {
        when(songLikeRepository.existsByIdUserIdAndIdSongId(USER_ID, SONG_ID)).thenReturn(false);
        assertThat(service.isLiked(USER_ID, SONG_ID)).isFalse();
    }

    // ── getLikedSongs ─────────────────────────────────────────────────────────

    @Test
    void getLikedSongs_mapsLikesToSongIds() {
        Pageable pageable = Pageable.ofSize(10);
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        SongLike l1 = SongLike.builder().id(new SongLikeId(USER_ID, s1)).build();
        SongLike l2 = SongLike.builder().id(new SongLikeId(USER_ID, s2)).build();
        Page<SongLike> page = new PageImpl<>(List.of(l1, l2));
        when(songLikeRepository.findAllByUserIdOrderByCreatedAtDesc(USER_ID, pageable))
                .thenReturn(page);

        Page<UUID> result = service.getLikedSongs(USER_ID, pageable);

        assertThat(result.getContent()).containsExactly(s1, s2);
    }

    // ── hideSong ──────────────────────────────────────────────────────────────

    @Test
    void hideSong_newHide_persists() {
        UUID albumId = UUID.randomUUID();
        when(hiddenSongRepository.existsByUserIdAndSongIdAndAlbumId(USER_ID, SONG_ID, albumId))
                .thenReturn(false);

        service.hideSong(USER_ID, SONG_ID, albumId);

        verify(hiddenSongRepository).save(any(HiddenSong.class));
    }

    @Test
    void hideSong_alreadyHidden_isIdempotent() {
        UUID albumId = UUID.randomUUID();
        when(hiddenSongRepository.existsByUserIdAndSongIdAndAlbumId(USER_ID, SONG_ID, albumId))
                .thenReturn(true);

        service.hideSong(USER_ID, SONG_ID, albumId);

        verify(hiddenSongRepository, never()).save(any());
    }

    // ── unhideSong ────────────────────────────────────────────────────────────

    @Test
    void unhideSong_existing_deletes() {
        UUID albumId = UUID.randomUUID();
        HiddenSong hidden = HiddenSong.builder()
                .userId(USER_ID).songId(SONG_ID).albumId(albumId).build();
        when(hiddenSongRepository.findByUserIdAndSongIdAndAlbumId(USER_ID, SONG_ID, albumId))
                .thenReturn(Optional.of(hidden));

        service.unhideSong(USER_ID, SONG_ID, albumId);

        verify(hiddenSongRepository).delete(hidden);
    }

    @Test
    void unhideSong_notHidden_doesNothing() {
        UUID albumId = UUID.randomUUID();
        when(hiddenSongRepository.findByUserIdAndSongIdAndAlbumId(USER_ID, SONG_ID, albumId))
                .thenReturn(Optional.empty());

        service.unhideSong(USER_ID, SONG_ID, albumId);

        verify(hiddenSongRepository, never()).delete(any());
    }

    // ── getHiddenSongsByAlbum ─────────────────────────────────────────────────

    @Test
    void getHiddenSongsByAlbum_mapsToSongIds() {
        UUID albumId = UUID.randomUUID();
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        HiddenSong h1 = HiddenSong.builder().userId(USER_ID).songId(s1).albumId(albumId).build();
        HiddenSong h2 = HiddenSong.builder().userId(USER_ID).songId(s2).albumId(albumId).build();
        when(hiddenSongRepository.findAllByUserIdAndAlbumId(USER_ID, albumId))
                .thenReturn(List.of(h1, h2));

        List<UUID> result = service.getHiddenSongsByAlbum(USER_ID, albumId);

        assertThat(result).containsExactly(s1, s2);
    }
}
