package com.rubymusic.interaction.controller;

import com.rubymusic.interaction.dto.LibraryItemType;
import com.rubymusic.interaction.dto.LibraryRequest;
import com.rubymusic.interaction.dto.UuidPage;
import com.rubymusic.interaction.service.UserLibraryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LibraryController implements LibraryApi {

    private final UserLibraryService libraryService;
    private final HttpServletRequest httpRequest;

    private UUID currentUserId() {
        return UUID.fromString(httpRequest.getHeader("X-User-Id"));
    }

    @Override
    public ResponseEntity<UuidPage> getLibrary(LibraryItemType type, Integer page, Integer size) {
        var entityType = com.rubymusic.interaction.model.enums.LibraryItemType.valueOf(type.name());
        Page<UUID> p = libraryService.getLibrary(currentUserId(), entityType, PageRequest.of(page, size));
        return ResponseEntity.ok(new UuidPage()
                .content(p.getContent())
                .totalElements((int) p.getTotalElements())
                .totalPages(p.getTotalPages())
                .page(p.getNumber())
                .size(p.getSize()));
    }

    @Override
    public ResponseEntity<Void> addToLibrary(LibraryRequest body) {
        var entityType = com.rubymusic.interaction.model.enums.LibraryItemType.valueOf(body.getType().name());
        libraryService.addToLibrary(currentUserId(), entityType, body.getItemId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeFromLibrary(LibraryItemType type, UUID itemId) {
        var entityType = com.rubymusic.interaction.model.enums.LibraryItemType.valueOf(type.name());
        libraryService.removeFromLibrary(currentUserId(), entityType, itemId);
        return ResponseEntity.noContent().build();
    }
}
