package com.rubymusic.interaction.service;

import java.util.List;
import java.util.UUID;

public interface UserPreferenceService {

    /** Replaces all genre preferences for the user (onboarding / update) */
    void saveGenrePreferences(UUID userId, List<UUID> genreIds);

    List<UUID> getGenrePreferences(UUID userId);

    /** Replaces all artist preferences for the user */
    void saveArtistPreferences(UUID userId, List<UUID> artistIds);

    List<UUID> getArtistPreferences(UUID userId);
}
