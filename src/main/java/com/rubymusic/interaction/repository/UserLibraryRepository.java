package com.rubymusic.interaction.repository;

import com.rubymusic.interaction.model.UserLibrary;
import com.rubymusic.interaction.model.enums.LibraryItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLibraryRepository extends JpaRepository<UserLibrary, UUID> {

    Page<UserLibrary> findAllByUserIdAndItemTypeOrderByAddedAtDesc(UUID userId, LibraryItemType itemType, Pageable pageable);

    Optional<UserLibrary> findByUserIdAndItemTypeAndItemId(UUID userId, LibraryItemType itemType, UUID itemId);

    boolean existsByUserIdAndItemTypeAndItemId(UUID userId, LibraryItemType itemType, UUID itemId);
}
