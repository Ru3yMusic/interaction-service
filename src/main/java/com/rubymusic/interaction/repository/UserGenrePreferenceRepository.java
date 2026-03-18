package com.rubymusic.interaction.repository;

import com.rubymusic.interaction.model.UserGenrePreference;
import com.rubymusic.interaction.model.id.UserGenrePreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserGenrePreferenceRepository extends JpaRepository<UserGenrePreference, UserGenrePreferenceId> {

    List<UserGenrePreference> findAllByUserId(UUID userId);

    boolean existsByIdUserIdAndIdGenreId(UUID userId, UUID genreId);

    @Modifying
    @Query("DELETE FROM UserGenrePreference p WHERE p.id.userId = :userId")
    void deleteAllByUserId(UUID userId);
}
