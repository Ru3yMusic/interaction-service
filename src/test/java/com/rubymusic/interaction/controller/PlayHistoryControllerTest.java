package com.rubymusic.interaction.controller;

import com.rubymusic.interaction.dto.PlayHistoryResponse;
import com.rubymusic.interaction.exception.GlobalExceptionHandler;
import com.rubymusic.interaction.mapper.PlayHistoryMapper;
import com.rubymusic.interaction.model.PlayHistory;
import com.rubymusic.interaction.model.UserPlaybackCheckpoint;
import com.rubymusic.interaction.service.PlayHistoryService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlayHistoryControllerTest {

    @Mock
    private PlayHistoryService playHistoryService;

    @Mock
    private PlayHistoryMapper playHistoryMapper;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private PlayHistoryController controller;

    private MockMvc mockMvc;

    private static final UUID USER = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        lenient().when(httpRequest.getHeader("X-User-Id")).thenReturn(USER.toString());
    }

    // ── getPlayHistory ────────────────────────────────────────────────────────

    @Test
    void getPlayHistory_returns200_withMappedPage() throws Exception {
        Page<PlayHistory> page = new PageImpl<>(List.of(mock(PlayHistory.class)));
        when(playHistoryService.getPlayHistory(eq(USER), any(Pageable.class))).thenReturn(page);
        when(playHistoryMapper.toDtoList(anyList())).thenReturn(List.of(new PlayHistoryResponse()));

        mockMvc.perform(get("/api/v1/interactions/play-history")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    // ── recordPlay ────────────────────────────────────────────────────────────

    @Test
    void recordPlay_returns204() throws Exception {
        UUID songId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/interactions/play-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"songId\":\"" + songId + "\",\"durationPlayedSeconds\":120}"))
                .andExpect(status().isNoContent());

        verify(playHistoryService).recordPlay(USER, songId, 120);
    }

    @Test
    void recordPlay_negativeDuration_returns422() throws Exception {
        // RecordPlayRequest.durationPlayedSeconds has @Min(0)
        UUID songId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/interactions/play-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"songId\":\"" + songId + "\",\"durationPlayedSeconds\":-5}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ── getPlaybackCheckpoint ─────────────────────────────────────────────────

    @Test
    void getPlaybackCheckpoint_existing_returns200() throws Exception {
        UUID songId = UUID.randomUUID();
        UserPlaybackCheckpoint checkpoint = UserPlaybackCheckpoint.builder()
                .userId(USER)
                .songId(songId)
                .currentTimeSeconds(45)
                .updatedAt(LocalDateTime.now())
                .build();
        when(playHistoryService.getPlaybackCheckpoint(USER)).thenReturn(Optional.of(checkpoint));

        mockMvc.perform(get("/api/v1/interactions/play-history/checkpoint"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songId").value(songId.toString()))
                .andExpect(jsonPath("$.currentTimeSeconds").value(45));
    }

    @Test
    void getPlaybackCheckpoint_absent_returns204() throws Exception {
        when(playHistoryService.getPlaybackCheckpoint(USER)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/interactions/play-history/checkpoint"))
                .andExpect(status().isNoContent());
    }

    // ── upsertPlaybackCheckpoint ──────────────────────────────────────────────

    @Test
    void upsertPlaybackCheckpoint_returns204() throws Exception {
        UUID songId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/interactions/play-history/checkpoint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"songId\":\"" + songId + "\",\"currentTimeSeconds\":60}"))
                .andExpect(status().isNoContent());

        verify(playHistoryService).savePlaybackCheckpoint(USER, songId, 60);
    }

    @Test
    void upsertPlaybackCheckpoint_negativeTime_returns422() throws Exception {
        UUID songId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/interactions/play-history/checkpoint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"songId\":\"" + songId + "\",\"currentTimeSeconds\":-1}"))
                .andExpect(status().isUnprocessableEntity());
    }
}
