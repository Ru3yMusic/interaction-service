package com.rubymusic.interaction.service.impl;

import com.rubymusic.interaction.model.UserLibrary;
import com.rubymusic.interaction.model.enums.LibraryItemType;
import com.rubymusic.interaction.repository.UserLibraryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserLibraryServiceImplTest {

    @Mock
    private UserLibraryRepository userLibraryRepository;

    @InjectMocks
    private UserLibraryServiceImpl service;

    private static final UUID USER = UUID.randomUUID();
    private static final UUID ITEM = UUID.randomUUID();

    // ── addToLibrary ──────────────────────────────────────────────────────────

    @Test
    void addToLibrary_newItem_persists() {
        when(userLibraryRepository.existsByUserIdAndItemTypeAndItemId(USER, LibraryItemType.ALBUM, ITEM))
                .thenReturn(false);

        service.addToLibrary(USER, LibraryItemType.ALBUM, ITEM);

        verify(userLibraryRepository).save(any(UserLibrary.class));
    }

    @Test
    void addToLibrary_alreadyInLibrary_isIdempotent() {
        when(userLibraryRepository.existsByUserIdAndItemTypeAndItemId(USER, LibraryItemType.ALBUM, ITEM))
                .thenReturn(true);

        service.addToLibrary(USER, LibraryItemType.ALBUM, ITEM);

        verify(userLibraryRepository, never()).save(any());
    }

    // ── removeFromLibrary ─────────────────────────────────────────────────────

    @Test
    void removeFromLibrary_existing_deletes() {
        UserLibrary entry = UserLibrary.builder()
                .userId(USER)
                .itemType(LibraryItemType.ARTIST)
                .itemId(ITEM)
                .build();
        when(userLibraryRepository.findByUserIdAndItemTypeAndItemId(USER, LibraryItemType.ARTIST, ITEM))
                .thenReturn(Optional.of(entry));

        service.removeFromLibrary(USER, LibraryItemType.ARTIST, ITEM);

        verify(userLibraryRepository).delete(entry);
    }

    @Test
    void removeFromLibrary_notInLibrary_doesNothing() {
        when(userLibraryRepository.findByUserIdAndItemTypeAndItemId(USER, LibraryItemType.ARTIST, ITEM))
                .thenReturn(Optional.empty());

        service.removeFromLibrary(USER, LibraryItemType.ARTIST, ITEM);

        verify(userLibraryRepository, never()).delete(any());
    }

    // ── getLibrary ────────────────────────────────────────────────────────────

    @Test
    void getLibrary_returnsItemIdsFromEntries() {
        Pageable pageable = Pageable.ofSize(10);
        UUID itemId1 = UUID.randomUUID();
        UUID itemId2 = UUID.randomUUID();
        UserLibrary e1 = UserLibrary.builder().userId(USER).itemType(LibraryItemType.ALBUM).itemId(itemId1).build();
        UserLibrary e2 = UserLibrary.builder().userId(USER).itemType(LibraryItemType.ALBUM).itemId(itemId2).build();
        Page<UserLibrary> page = new PageImpl<>(List.of(e1, e2));

        when(userLibraryRepository.findAllByUserIdAndItemTypeOrderByAddedAtDesc(USER, LibraryItemType.ALBUM, pageable))
                .thenReturn(page);

        Page<UUID> result = service.getLibrary(USER, LibraryItemType.ALBUM, pageable);

        assertThat(result.getContent()).containsExactly(itemId1, itemId2);
    }

    // ── isInLibrary ───────────────────────────────────────────────────────────

    @Test
    void isInLibrary_present_returnsTrue() {
        when(userLibraryRepository.existsByUserIdAndItemTypeAndItemId(USER, LibraryItemType.ALBUM, ITEM))
                .thenReturn(true);

        assertThat(service.isInLibrary(USER, LibraryItemType.ALBUM, ITEM)).isTrue();
    }

    @Test
    void isInLibrary_absent_returnsFalse() {
        when(userLibraryRepository.existsByUserIdAndItemTypeAndItemId(USER, LibraryItemType.ARTIST, ITEM))
                .thenReturn(false);

        assertThat(service.isInLibrary(USER, LibraryItemType.ARTIST, ITEM)).isFalse();
    }
}
