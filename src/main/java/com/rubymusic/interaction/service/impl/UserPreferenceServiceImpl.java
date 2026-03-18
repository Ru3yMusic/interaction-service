package com.rubymusic.interaction.service.impl;

import com.rubymusic.interaction.model.UserArtistPreference;
import com.rubymusic.interaction.model.UserGenrePreference;
import com.rubymusic.interaction.model.id.UserArtistPreferenceId;
import com.rubymusic.interaction.model.id.UserGenrePreferenceId;
import com.rubymusic.interaction.repository.UserArtistPreferenceRepository;
import com.rubymusic.interaction.repository.UserGenrePreferenceRepository;
import com.rubymusic.interaction.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserGenrePreferenceRepository genrePreferenceRepository;
    private final UserArtistPreferenceRepository artistPreferenceRepository;

    @Override
    @Transactional
    public void saveGenrePreferences(UUID userId, List<UUID> genreIds) {
        genrePreferenceRepository.deleteAllByUserId(userId);
        List<UserGenrePreference> preferences = genreIds.stream()
                .map(genreId -> UserGenrePreference.builder()
                        .id(new UserGenrePreferenceId(userId, genreId))
                        .build())
                .toList();
        genrePreferenceRepository.saveAll(preferences);
    }

    @Override
    public List<UUID> getGenrePreferences(UUID userId) {
        return genrePreferenceRepository.findAllByUserId(userId)
                .stream()
                .map(p -> p.getId().getGenreId())
                .toList();
    }

    @Override
    @Transactional
    public void saveArtistPreferences(UUID userId, List<UUID> artistIds) {
        artistPreferenceRepository.deleteAllByUserId(userId);
        List<UserArtistPreference> preferences = artistIds.stream()
                .map(artistId -> UserArtistPreference.builder()
                        .id(new UserArtistPreferenceId(userId, artistId))
                        .build())
                .toList();
        artistPreferenceRepository.saveAll(preferences);
    }

    @Override
    public List<UUID> getArtistPreferences(UUID userId) {
        return artistPreferenceRepository.findAllByUserId(userId)
                .stream()
                .map(p -> p.getId().getArtistId())
                .toList();
    }
}
