package com.rubymusic.interaction.service.impl;

import com.rubymusic.interaction.model.HiddenSong;
import com.rubymusic.interaction.model.SongLike;
import com.rubymusic.interaction.model.id.SongLikeId;
import com.rubymusic.interaction.repository.HiddenSongRepository;
import com.rubymusic.interaction.repository.SongLikeRepository;
import com.rubymusic.interaction.service.SongInteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongInteractionServiceImpl implements SongInteractionService {

    private static final String TOPIC_SONG_LIKED    = "song.liked";
    private static final String TOPIC_SONG_UNLIKED  = "song.unliked";

    private final SongLikeRepository songLikeRepository;
    private final HiddenSongRepository hiddenSongRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    public void likeSong(UUID userId, UUID songId) {
        if (songLikeRepository.existsByIdUserIdAndIdSongId(userId, songId)) {
            return; // idempotent
        }
        SongLike like = SongLike.builder()
                .id(new SongLikeId(userId, songId))
                .build();
        songLikeRepository.save(like);

        // Notify catalog-service to increment likes_count
        kafkaTemplate.send(TOPIC_SONG_LIKED, songId.toString());
        log.debug("Song liked: user={} song={}", userId, songId);
    }

    @Override
    @Transactional
    public void unlikeSong(UUID userId, UUID songId) {
        SongLikeId id = new SongLikeId(userId, songId);
        if (songLikeRepository.existsById(id)) {
            songLikeRepository.deleteById(id);
            kafkaTemplate.send(TOPIC_SONG_UNLIKED, songId.toString());
        }
    }

    @Override
    public boolean isLiked(UUID userId, UUID songId) {
        return songLikeRepository.existsByIdUserIdAndIdSongId(userId, songId);
    }

    @Override
    public Page<UUID> getLikedSongs(UUID userId, Pageable pageable) {
        return songLikeRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(like -> like.getId().getSongId());
    }

    @Override
    @Transactional
    public void hideSong(UUID userId, UUID songId, UUID albumId) {
        if (hiddenSongRepository.existsByUserIdAndSongIdAndAlbumId(userId, songId, albumId)) {
            return; // idempotent
        }
        HiddenSong hidden = HiddenSong.builder()
                .userId(userId)
                .songId(songId)
                .albumId(albumId)
                .build();
        hiddenSongRepository.save(hidden);
    }

    @Override
    @Transactional
    public void unhideSong(UUID userId, UUID songId, UUID albumId) {
        hiddenSongRepository.findByUserIdAndSongIdAndAlbumId(userId, songId, albumId)
                .ifPresent(hiddenSongRepository::delete);
    }

    @Override
    public List<UUID> getHiddenSongsByAlbum(UUID userId, UUID albumId) {
        return hiddenSongRepository.findAllByUserIdAndAlbumId(userId, albumId)
                .stream()
                .map(HiddenSong::getSongId)
                .toList();
    }
}
