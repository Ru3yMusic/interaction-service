package com.rubymusic.interaction.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserGenrePreferenceId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "genre_id")
    private UUID genreId;
}
