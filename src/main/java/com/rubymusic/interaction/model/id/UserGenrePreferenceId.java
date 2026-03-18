package com.rubymusic.interaction.model.id;

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
    private UUID userId;
    private UUID genreId;
}
