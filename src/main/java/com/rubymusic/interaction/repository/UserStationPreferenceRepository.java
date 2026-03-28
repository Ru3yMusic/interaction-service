package com.rubymusic.interaction.repository;

import com.rubymusic.interaction.model.UserStationPreference;
import com.rubymusic.interaction.model.id.UserStationPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserStationPreferenceRepository extends JpaRepository<UserStationPreference, UserStationPreferenceId> {

    List<UserStationPreference> findAllByUserId(UUID userId);

    boolean existsByIdUserIdAndIdStationId(UUID userId, UUID stationId);

    @Modifying
    @Query("DELETE FROM UserStationPreference p WHERE p.id.userId = :userId")
    void deleteAllByUserId(UUID userId);
}
