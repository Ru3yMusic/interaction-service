package com.rubymusic.interaction.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "play_history", indexes = {
        @Index(name = "idx_ph_user_id_played_at", columnList = "user_id, played_at DESC")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** References catalog-service song — no cross-service FK */
    @Column(name = "song_id", nullable = false)
    private UUID songId;

    @Column(name = "played_at", nullable = false)
    @Builder.Default
    private LocalDateTime playedAt = LocalDateTime.now();

    /** Seconds actually listened — used for recommendation weighting */
    @Column(name = "duration_played", nullable = false)
    private Integer durationPlayed;
}
