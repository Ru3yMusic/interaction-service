package com.rubymusic.interaction.controller;

import com.rubymusic.interaction.client.auth.api.InternalAuthApi;
import com.rubymusic.interaction.client.playlist.api.InternalPlaylistApi;
import com.rubymusic.interaction.service.PlayHistoryService;
import com.rubymusic.interaction.service.SongInteractionService;
import com.rubymusic.interaction.service.UserLibraryService;
import com.rubymusic.interaction.service.UserPreferenceService;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link InternalInteractionController}.
 *
 * TDD cycle:
 * RED  — written before InternalInteractionController (and InternalApi interface) existed
 * GREEN — openapi.yml updated → InternalApi generated → controller created; tests pass
 * TRIANGULATE — covers pagination, multiple songs, security rejections
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InternalInteractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KeyPair testKeyPair;

    @MockBean
    private SongInteractionService songInteractionService;

    @MockBean
    private UserPreferenceService userPreferenceService;

    @MockBean
    private PlayHistoryService playHistoryService;

    @MockBean
    private UserLibraryService userLibraryService;

    @MockBean
    @SuppressWarnings("rawtypes")
    private KafkaTemplate kafkaTemplate;

    @MockBean
    private InternalPlaylistApi internalPlaylistApi;

    @MockBean
    private InternalAuthApi internalAuthApi;

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void getUserLikes_serviceJwt_returnsPaginatedSongIds() throws Exception {
        UUID userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID songId1 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID songId2 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        when(songInteractionService.getLikedSongs(eq(userId), any()))
                .thenReturn(new PageImpl<>(List.of(songId1, songId2), PageRequest.of(0, 20), 2));

        mockMvc.perform(get("/api/internal/v1/users/" + userId + "/likes")
                        .header("Authorization", "Bearer " + serviceToken())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songIds").isArray())
                .andExpect(jsonPath("$.songIds[0]").value(songId1.toString()))
                .andExpect(jsonPath("$.songIds[1]").value(songId2.toString()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    // TRIANGULATE: empty likes list is also valid
    @Test
    void getUserLikes_serviceJwt_noLikes_returnsEmptyList() throws Exception {
        UUID userId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

        when(songInteractionService.getLikedSongs(eq(userId), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/internal/v1/users/" + userId + "/likes")
                        .header("Authorization", "Bearer " + serviceToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songIds").isArray())
                .andExpect(jsonPath("$.songIds").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ── Security rejections ───────────────────────────────────────────────────

    @Test
    void getUserLikes_userJwt_403() throws Exception {
        String userToken = Jwts.builder()
                .subject("user-123")
                .claim("role", "USER")
                .signWith(testKeyPair.getPrivate())
                .compact();

        mockMvc.perform(get("/api/internal/v1/users/" + UUID.randomUUID() + "/likes")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserLikes_noAuth_401() throws Exception {
        mockMvc.perform(get("/api/internal/v1/users/" + UUID.randomUUID() + "/likes"))
                .andExpect(status().isUnauthorized());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String serviceToken() {
        return Jwts.builder()
                .subject("catalog-service")
                .claim("role", "SERVICE")
                .signWith(testKeyPair.getPrivate())
                .compact();
    }
}
