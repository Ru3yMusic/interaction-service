package com.rubymusic.interaction.repository;

import com.rubymusic.interaction.model.HiddenSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HiddenSongRepository extends JpaRepository<HiddenSong, UUID> {

    List<HiddenSong> findAllByUserIdAndAlbumId(UUID userId, UUID albumId);

    Optional<HiddenSong> findByUserIdAndSongIdAndAlbumId(UUID userId, UUID songId, UUID albumId);

    boolean existsByUserIdAndSongIdAndAlbumId(UUID userId, UUID songId, UUID albumId);
}
