package com.rubymusic.interaction.model;

import com.rubymusic.interaction.model.id.UserArtistPreferenceId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_artist_preferences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserArtistPreference {

    @EmbeddedId
    private UserArtistPreferenceId id;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    @Column(name = "artist_id", insertable = false, updatable = false)
    private UUID artistId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
