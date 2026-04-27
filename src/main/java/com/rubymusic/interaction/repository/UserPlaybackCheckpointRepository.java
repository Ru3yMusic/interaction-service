package com.rubymusic.interaction.repository;

import com.rubymusic.interaction.model.UserPlaybackCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPlaybackCheckpointRepository extends JpaRepository<UserPlaybackCheckpoint, UUID> {
}
