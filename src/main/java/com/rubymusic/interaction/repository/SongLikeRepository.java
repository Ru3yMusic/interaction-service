package com.rubymusic.interaction.repository;

import com.rubymusic.interaction.model.SongLike;
import com.rubymusic.interaction.model.id.SongLikeId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SongLikeRepository extends JpaRepository<SongLike, SongLikeId> {

    Page<SongLike> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    boolean existsByIdUserIdAndIdSongId(UUID userId, UUID songId);

    long countByUserId(UUID userId);
}
