package com.rubymusic.interaction.controller;

import com.rubymusic.interaction.dto.UuidListRequest;
import com.rubymusic.interaction.service.UserPreferenceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PreferencesController implements PreferencesApi {

    private final UserPreferenceService preferenceService;
    private final HttpServletRequest httpRequest;

    private UUID currentUserId() {
        return UUID.fromString(httpRequest.getHeader("X-User-Id"));
    }

    @Override
    public ResponseEntity<List<UUID>> getGenrePreferences() {
        return ResponseEntity.ok(preferenceService.getGenrePreferences(currentUserId()));
    }

    @Override
    public ResponseEntity<Void> saveGenrePreferences(UuidListRequest body) {
        preferenceService.saveGenrePreferences(currentUserId(), body.getIds());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<UUID>> getArtistPreferences() {
        return ResponseEntity.ok(preferenceService.getArtistPreferences(currentUserId()));
    }

    @Override
    public ResponseEntity<Void> saveArtistPreferences(UuidListRequest body) {
        preferenceService.saveArtistPreferences(currentUserId(), body.getIds());
        return ResponseEntity.noContent().build();
    }
}
