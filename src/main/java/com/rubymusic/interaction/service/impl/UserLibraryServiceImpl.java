package com.rubymusic.interaction.service.impl;

import com.rubymusic.interaction.model.UserLibrary;
import com.rubymusic.interaction.model.enums.LibraryItemType;
import com.rubymusic.interaction.repository.UserLibraryRepository;
import com.rubymusic.interaction.service.UserLibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLibraryServiceImpl implements UserLibraryService {

    private final UserLibraryRepository userLibraryRepository;

    @Override
    @Transactional
    public void addToLibrary(UUID userId, LibraryItemType type, UUID itemId) {
        if (userLibraryRepository.existsByUserIdAndItemTypeAndItemId(userId, type, itemId)) {
            return; // idempotent
        }
        UserLibrary entry = UserLibrary.builder()
                .userId(userId)
                .itemType(type)
                .itemId(itemId)
                .build();
        userLibraryRepository.save(entry);
    }

    @Override
    @Transactional
    public void removeFromLibrary(UUID userId, LibraryItemType type, UUID itemId) {
        userLibraryRepository.findByUserIdAndItemTypeAndItemId(userId, type, itemId)
                .ifPresent(userLibraryRepository::delete);
    }

    @Override
    public Page<UUID> getLibrary(UUID userId, LibraryItemType type, Pageable pageable) {
        return userLibraryRepository
                .findAllByUserIdAndItemTypeOrderByAddedAtDesc(userId, type, pageable)
                .map(UserLibrary::getItemId);
    }

    @Override
    public boolean isInLibrary(UUID userId, LibraryItemType type, UUID itemId) {
        return userLibraryRepository.existsByUserIdAndItemTypeAndItemId(userId, type, itemId);
    }
}
