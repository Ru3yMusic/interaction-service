package com.rubymusic.interaction.controller;

import com.rubymusic.interaction.exception.GlobalExceptionHandler;
import com.rubymusic.interaction.model.enums.LibraryItemType;
import com.rubymusic.interaction.service.UserLibraryService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LibraryControllerTest {

    @Mock
    private UserLibraryService libraryService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private LibraryController controller;

    private MockMvc mockMvc;

    private static final UUID USER = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        lenient().when(httpRequest.getHeader("X-User-Id")).thenReturn(USER.toString());
    }

    // ── getLibrary ────────────────────────────────────────────────────────────

    @Test
    void getLibrary_returns200_withMappedPage() throws Exception {
        UUID itemId1 = UUID.randomUUID();
        UUID itemId2 = UUID.randomUUID();
        Page<UUID> page = new PageImpl<>(List.of(itemId1, itemId2));

        when(libraryService.getLibrary(eq(USER), eq(LibraryItemType.ALBUM), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/interactions/library")
                        .param("type", "ALBUM")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    // ── addToLibrary ──────────────────────────────────────────────────────────

    @Test
    void addToLibrary_returns204() throws Exception {
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/interactions/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"ALBUM\",\"itemId\":\"" + itemId + "\"}"))
                .andExpect(status().isNoContent());

        verify(libraryService).addToLibrary(USER, LibraryItemType.ALBUM, itemId);
    }

    @Test
    void addToLibrary_missingItemId_returns422() throws Exception {
        // LibraryRequest.itemId is @NotNull
        mockMvc.perform(post("/api/v1/interactions/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"ALBUM\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void addToLibrary_missingType_returns422() throws Exception {
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/interactions/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":\"" + itemId + "\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ── removeFromLibrary ─────────────────────────────────────────────────────

    @Test
    void removeFromLibrary_returns204() throws Exception {
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/interactions/library/{type}/{itemId}", "ARTIST", itemId))
                .andExpect(status().isNoContent());

        verify(libraryService).removeFromLibrary(USER, LibraryItemType.ARTIST, itemId);
    }
}
