package com.rubymusic.interaction.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hidden_songs",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_hidden_songs_user_song_album",
                columnNames = {"user_id", "song_id", "album_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HiddenSong {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** References catalog-service song — no cross-service FK */
    @Column(name = "song_id", nullable = false)
    private UUID songId;

    /** Scoped per album — hiding a song in one album does not affect others */
    @Column(name = "album_id", nullable = false)
    private UUID albumId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
