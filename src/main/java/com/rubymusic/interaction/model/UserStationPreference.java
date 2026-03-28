package com.rubymusic.interaction.model;

import com.rubymusic.interaction.model.id.UserStationPreferenceId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_station_preferences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStationPreference {

    @EmbeddedId
    private UserStationPreferenceId id;

    /** References auth-service user — no cross-service FK */
    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    /** References catalog-service Station — no cross-service FK */
    @Column(name = "station_id", insertable = false, updatable = false)
    private UUID stationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
