package com.rubymusic.interaction.service.impl;

import com.rubymusic.interaction.model.UserArtistPreference;
import com.rubymusic.interaction.model.UserGenrePreference;
import com.rubymusic.interaction.model.UserStationPreference;
import com.rubymusic.interaction.model.id.UserArtistPreferenceId;
import com.rubymusic.interaction.model.id.UserGenrePreferenceId;
import com.rubymusic.interaction.model.id.UserStationPreferenceId;
import com.rubymusic.interaction.repository.UserArtistPreferenceRepository;
import com.rubymusic.interaction.repository.UserGenrePreferenceRepository;
import com.rubymusic.interaction.repository.UserStationPreferenceRepository;
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
    private final UserStationPreferenceRepository stationPreferenceRepository;

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

    @Override
    @Transactional
    public void saveStationPreferences(UUID userId, List<UUID> stationIds) {
        stationPreferenceRepository.deleteAllByUserId(userId);
        List<UserStationPreference> preferences = stationIds.stream()
                .map(stationId -> UserStationPreference.builder()
                        .id(new UserStationPreferenceId(userId, stationId))
                        .build())
                .toList();
        stationPreferenceRepository.saveAll(preferences);
    }

    @Override
    public List<UUID> getStationPreferences(UUID userId) {
        return stationPreferenceRepository.findAllByUserId(userId)
                .stream()
                .map(p -> p.getId().getStationId())
                .toList();
    }
}
