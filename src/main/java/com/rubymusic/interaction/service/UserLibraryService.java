package com.rubymusic.interaction.service;

import com.rubymusic.interaction.model.enums.LibraryItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserLibraryService {

    void addToLibrary(UUID userId, LibraryItemType type, UUID itemId);

    void removeFromLibrary(UUID userId, LibraryItemType type, UUID itemId);

    /** Returns item IDs of the given type, ordered by most recently added */
    Page<UUID> getLibrary(UUID userId, LibraryItemType type, Pageable pageable);

    boolean isInLibrary(UUID userId, LibraryItemType type, UUID itemId);
}
