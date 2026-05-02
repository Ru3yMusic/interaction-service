package com.rubymusic.interaction.controller;

import com.rubymusic.interaction.exception.GlobalExceptionHandler;
import com.rubymusic.interaction.service.SongInteractionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SongInteractionsControllerTest {

    @Mock
    private SongInteractionService songInteractionService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private SongInteractionsController controller;

    private MockMvc mockMvc;

    private static final UUID USER = UUID.randomUUID();
    private static final UUID SONG = UUID.randomUUID();
    private static final UUID ALBUM = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        when(httpRequest.getHeader("X-User-Id")).thenReturn(USER.toString());
    }

    // ── like / unlike ─────────────────────────────────────────────────────────

    @Test
    void likeSong_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/interactions/songs/{songId}/like", SONG))
                .andExpect(status().isNoContent());

        verify(songInteractionService).likeSong(USER, SONG);
    }

    @Test
    void unlikeSong_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/interactions/songs/{songId}/like", SONG))
                .andExpect(status().isNoContent());

        verify(songInteractionService).unlikeSong(USER, SONG);
    }

    // ── like status ───────────────────────────────────────────────────────────

    @Test
    void getLikeStatus_liked_returnsTrue() throws Exception {
        when(songInteractionService.isLiked(USER, SONG)).thenReturn(true);

        mockMvc.perform(get("/api/v1/interactions/songs/{songId}/like/status", SONG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    void getLikeStatus_notLiked_returnsFalse() throws Exception {
        when(songInteractionService.isLiked(USER, SONG)).thenReturn(false);

        mockMvc.perform(get("/api/v1/interactions/songs/{songId}/like/status", SONG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));
    }

    // ── liked songs (paginated) ───────────────────────────────────────────────

    @Test
    void getLikedSongs_returns200_withPage() throws Exception {
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        Page<UUID> page = new PageImpl<>(List.of(s1, s2));
        when(songInteractionService.getLikedSongs(eq(USER), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/interactions/songs/liked")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    // ── hide / unhide ─────────────────────────────────────────────────────────

    @Test
    void hideSong_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/interactions/albums/{albumId}/songs/{songId}/hide", ALBUM, SONG))
                .andExpect(status().isNoContent());

        verify(songInteractionService).hideSong(USER, SONG, ALBUM);
    }

    @Test
    void unhideSong_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/interactions/albums/{albumId}/songs/{songId}/hide", ALBUM, SONG))
                .andExpect(status().isNoContent());

        verify(songInteractionService).unhideSong(USER, SONG, ALBUM);
    }

    @Test
    void getHiddenSongs_returnsList() throws Exception {
        UUID hidden1 = UUID.randomUUID();
        when(songInteractionService.getHiddenSongsByAlbum(USER, ALBUM)).thenReturn(List.of(hidden1));

        mockMvc.perform(get("/api/v1/interactions/albums/{albumId}/hidden-songs", ALBUM))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value(hidden1.toString()));
    }
}
