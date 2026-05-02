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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceImplTest {

    @Mock
    private UserGenrePreferenceRepository genrePreferenceRepository;

    @Mock
    private UserArtistPreferenceRepository artistPreferenceRepository;

    @Mock
    private UserStationPreferenceRepository stationPreferenceRepository;

    @InjectMocks
    private UserPreferenceServiceImpl service;

    private static final UUID USER = UUID.randomUUID();

    // ── saveGenrePreferences ──────────────────────────────────────────────────

    @Test
    void saveGenrePreferences_replacesAllExisting() {
        UUID g1 = UUID.randomUUID();
        UUID g2 = UUID.randomUUID();

        service.saveGenrePreferences(USER, List.of(g1, g2));

        verify(genrePreferenceRepository).deleteAllByUserId(USER);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserGenrePreference>> captor = ArgumentCaptor.forClass(List.class);
        verify(genrePreferenceRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    void saveGenrePreferences_emptyList_clearsAndSavesNothing() {
        service.saveGenrePreferences(USER, List.of());

        verify(genrePreferenceRepository).deleteAllByUserId(USER);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserGenrePreference>> captor = ArgumentCaptor.forClass(List.class);
        verify(genrePreferenceRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).isEmpty();
    }

    // ── getGenrePreferences ───────────────────────────────────────────────────

    @Test
    void getGenrePreferences_returnsGenreIds() {
        UUID g1 = UUID.randomUUID();
        UUID g2 = UUID.randomUUID();
        UserGenrePreference p1 = UserGenrePreference.builder()
                .id(new UserGenrePreferenceId(USER, g1)).build();
        UserGenrePreference p2 = UserGenrePreference.builder()
                .id(new UserGenrePreferenceId(USER, g2)).build();
        when(genrePreferenceRepository.findAllByUserId(USER)).thenReturn(List.of(p1, p2));

        List<UUID> result = service.getGenrePreferences(USER);

        assertThat(result).containsExactly(g1, g2);
    }

    // ── saveArtistPreferences ─────────────────────────────────────────────────

    @Test
    void saveArtistPreferences_replacesAllExisting() {
        UUID a1 = UUID.randomUUID();

        service.saveArtistPreferences(USER, List.of(a1));

        verify(artistPreferenceRepository).deleteAllByUserId(USER);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserArtistPreference>> captor = ArgumentCaptor.forClass(List.class);
        verify(artistPreferenceRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    // ── getArtistPreferences ──────────────────────────────────────────────────

    @Test
    void getArtistPreferences_returnsArtistIds() {
        UUID a1 = UUID.randomUUID();
        UserArtistPreference p1 = UserArtistPreference.builder()
                .id(new UserArtistPreferenceId(USER, a1)).build();
        when(artistPreferenceRepository.findAllByUserId(USER)).thenReturn(List.of(p1));

        List<UUID> result = service.getArtistPreferences(USER);

        assertThat(result).containsExactly(a1);
    }

    // ── saveStationPreferences ────────────────────────────────────────────────

    @Test
    void saveStationPreferences_replacesAllExisting() {
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();

        service.saveStationPreferences(USER, List.of(s1, s2));

        verify(stationPreferenceRepository).deleteAllByUserId(USER);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserStationPreference>> captor = ArgumentCaptor.forClass(List.class);
        verify(stationPreferenceRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    // ── getStationPreferences ─────────────────────────────────────────────────

    @Test
    void getStationPreferences_returnsStationIds() {
        UUID s1 = UUID.randomUUID();
        UserStationPreference p1 = UserStationPreference.builder()
                .id(new UserStationPreferenceId(USER, s1)).build();
        when(stationPreferenceRepository.findAllByUserId(USER)).thenReturn(List.of(p1));

        List<UUID> result = service.getStationPreferences(USER);

        assertThat(result).containsExactly(s1);
    }
}
