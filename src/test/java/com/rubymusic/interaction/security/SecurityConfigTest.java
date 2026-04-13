package com.rubymusic.interaction.security;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security filter chain integration tests — 401/403/200 matrix.
 *
 * TDD cycle:
 * RED  — written before SecurityConfig existed (Spring Boot default security
 *         does not match the required allow-list — actuator would return 401, etc.)
 * GREEN — SecurityConfig + JwtAuthenticationFilter created; all scenarios pass
 * TRIANGULATE — covers: no-auth, USER JWT, SERVICE JWT, public paths, internal paths
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

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

    // ── Public paths ──────────────────────────────────────────────────────────

    @Test
    void actuatorHealth_noAuth_200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void apiDocs_noAuth_200() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    // ── /api/v1/** requires authentication ────────────────────────────────────

    @Test
    void apiV1_noAuth_401() throws Exception {
        mockMvc.perform(get("/api/v1/interactions/songs/liked"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiV1_userJwt_200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(songInteractionService.getLikedSongs(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/v1/interactions/songs/liked")
                        .header("Authorization", "Bearer " + buildToken("user-123", "USER"))
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());
    }

    // ── /api/internal/v1/** requires SERVICE role ─────────────────────────────

    @Test
    void apiInternalV1_noAuth_401() throws Exception {
        mockMvc.perform(get("/api/internal/v1/users/" + UUID.randomUUID() + "/likes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiInternalV1_userJwt_403() throws Exception {
        mockMvc.perform(get("/api/internal/v1/users/" + UUID.randomUUID() + "/likes")
                        .header("Authorization", "Bearer " + buildToken("user-123", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void apiInternalV1_serviceJwt_200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(songInteractionService.getLikedSongs(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/internal/v1/users/" + userId + "/likes")
                        .header("Authorization", "Bearer " + buildToken("playlist-service", "SERVICE")))
                .andExpect(status().isOk());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String buildToken(String subject, String role) {
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .signWith(testKeyPair.getPrivate())
                .compact();
    }
}
