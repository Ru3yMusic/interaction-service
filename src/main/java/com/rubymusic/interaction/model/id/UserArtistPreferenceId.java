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
public class UserArtistPreferenceId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "artist_id")
    private UUID artistId;
}
