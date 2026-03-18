package com.rubymusic.interaction.model;

import com.rubymusic.interaction.model.enums.LibraryItemType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_library",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_library_user_type_item",
                columnNames = {"user_id", "item_type", "item_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLibrary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 10)
    private LibraryItemType itemType;

    /** ID of the album or artist in catalog-service */
    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "added_at", nullable = false)
    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();
}
