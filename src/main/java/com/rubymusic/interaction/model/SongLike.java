package com.rubymusic.interaction.model;

import com.rubymusic.interaction.model.id.SongLikeId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "song_likes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongLike {

    @EmbeddedId
    private SongLikeId id;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    @Column(name = "song_id", insertable = false, updatable = false)
    private UUID songId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
