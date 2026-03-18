package com.rubymusic.interaction.repository;

import com.rubymusic.interaction.model.PlayHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistory, UUID> {

    Page<PlayHistory> findAllByUserIdOrderByPlayedAtDesc(UUID userId, Pageable pageable);
}
