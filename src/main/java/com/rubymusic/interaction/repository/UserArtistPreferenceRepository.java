package com.rubymusic.interaction.repository;

import com.rubymusic.interaction.model.UserArtistPreference;
import com.rubymusic.interaction.model.id.UserArtistPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserArtistPreferenceRepository extends JpaRepository<UserArtistPreference, UserArtistPreferenceId> {

    List<UserArtistPreference> findAllByUserId(UUID userId);

    boolean existsByIdUserIdAndIdArtistId(UUID userId, UUID artistId);

    @Modifying
    @Query("DELETE FROM UserArtistPreference p WHERE p.id.userId = :userId")
    void deleteAllByUserId(UUID userId);
}
