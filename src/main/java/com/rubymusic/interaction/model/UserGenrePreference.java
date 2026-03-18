package com.rubymusic.interaction.model;

import com.rubymusic.interaction.model.id.UserGenrePreferenceId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_genre_preferences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGenrePreference {

    @EmbeddedId
    private UserGenrePreferenceId id;

    /** References catalog-service — no cross-service FK */
    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    @Column(name = "genre_id", insertable = false, updatable = false)
    private UUID genreId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
