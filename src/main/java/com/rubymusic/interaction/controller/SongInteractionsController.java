package com.rubymusic.interaction.controller;

import com.rubymusic.interaction.dto.LikeStatusResponse;
import com.rubymusic.interaction.dto.UuidPage;
import com.rubymusic.interaction.service.SongInteractionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SongInteractionsController implements SongInteractionsApi {

    private final SongInteractionService songInteractionService;
    private final HttpServletRequest httpRequest;

    @Override
    public Optional<HttpServletRequest> getRequest() {
        return Optional.of(httpRequest);
    }

    private UUID currentUserId() {
        return UUID.fromString(httpRequest.getHeader("X-User-Id"));
    }

    @Override
    public ResponseEntity<Void> likeSong(UUID songId) {
        songInteractionService.likeSong(currentUserId(), songId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> unlikeSong(UUID songId) {
        songInteractionService.unlikeSong(currentUserId(), songId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<LikeStatusResponse> getLikeStatus(UUID songId) {
        boolean liked = songInteractionService.isLiked(currentUserId(), songId);
        return ResponseEntity.ok(new LikeStatusResponse().liked(liked));
    }

    @Override
    public ResponseEntity<UuidPage> getLikedSongs(Integer page, Integer size) {
        Page<UUID> p = songInteractionService.getLikedSongs(currentUserId(), PageRequest.of(page, size));
        return ResponseEntity.ok(toUuidPage(p));
    }

    @Override
    public ResponseEntity<Void> hideSong(UUID albumId, UUID songId) {
        songInteractionService.hideSong(currentUserId(), songId, albumId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> unhideSong(UUID albumId, UUID songId) {
        songInteractionService.unhideSong(currentUserId(), songId, albumId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<UUID>> getHiddenSongs(UUID albumId) {
        return ResponseEntity.ok(songInteractionService.getHiddenSongsByAlbum(currentUserId(), albumId));
    }

    private UuidPage toUuidPage(Page<UUID> p) {
        return new UuidPage()
                .content(p.getContent())
                .totalElements((int) p.getTotalElements())
                .totalPages(p.getTotalPages())
                .page(p.getNumber())
                .size(p.getSize());
    }
}
