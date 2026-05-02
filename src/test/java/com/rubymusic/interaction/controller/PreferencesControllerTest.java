package com.rubymusic.interaction.controller;

import com.rubymusic.interaction.exception.GlobalExceptionHandler;
import com.rubymusic.interaction.service.UserPreferenceService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PreferencesControllerTest {

    @Mock
    private UserPreferenceService preferenceService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private PreferencesController controller;

    private MockMvc mockMvc;

    private static final UUID USER = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        lenient().when(httpRequest.getHeader("X-User-Id")).thenReturn(USER.toString());
    }

    // ── genres ────────────────────────────────────────────────────────────────

    @Test
    void getGenrePreferences_returnsList() throws Exception {
        UUID g1 = UUID.randomUUID();
        UUID g2 = UUID.randomUUID();
        when(preferenceService.getGenrePreferences(USER)).thenReturn(List.of(g1, g2));

        mockMvc.perform(get("/api/v1/interactions/preferences/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void saveGenrePreferences_returns204() throws Exception {
        UUID g1 = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/interactions/preferences/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"" + g1 + "\"]}"))
                .andExpect(status().isNoContent());

        verify(preferenceService).saveGenrePreferences(USER, List.of(g1));
    }

    // ── artists ───────────────────────────────────────────────────────────────

    @Test
    void getArtistPreferences_returnsList() throws Exception {
        UUID a1 = UUID.randomUUID();
        when(preferenceService.getArtistPreferences(USER)).thenReturn(List.of(a1));

        mockMvc.perform(get("/api/v1/interactions/preferences/artists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void saveArtistPreferences_returns204() throws Exception {
        UUID a1 = UUID.randomUUID();
        UUID a2 = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/interactions/preferences/artists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"" + a1 + "\",\"" + a2 + "\"]}"))
                .andExpect(status().isNoContent());

        verify(preferenceService).saveArtistPreferences(USER, List.of(a1, a2));
    }

    // ── stations ──────────────────────────────────────────────────────────────

    @Test
    void getStationPreferences_returnsList() throws Exception {
        when(preferenceService.getStationPreferences(USER)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/interactions/preferences/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void saveStationPreferences_returns204() throws Exception {
        UUID s1 = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/interactions/preferences/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"" + s1 + "\"]}"))
                .andExpect(status().isNoContent());

        verify(preferenceService).saveStationPreferences(USER, List.of(s1));
    }

    // ── validation ────────────────────────────────────────────────────────────

    @Test
    void saveGenrePreferences_emptyIds_returns422() throws Exception {
        // UuidListRequest.ids has @NotNull @Size(min=1)
        mockMvc.perform(post("/api/v1/interactions/preferences/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[]}"))
                .andExpect(status().isUnprocessableEntity());
    }
}
