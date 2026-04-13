package com.rubymusic.interaction.controller;

import com.rubymusic.interaction.dto.UserLikesInternalResponse;
import com.rubymusic.interaction.service.SongInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Implements the generated {@code InternalApi} interface for service-to-service endpoints.
 *
 * <p>Requires {@code ROLE_SERVICE} JWT — enforced by {@link com.rubymusic.interaction.config.SecurityConfig}.
 * The {@code userId} is taken from the path parameter (not the JWT subject), allowing any
 * authorized service to query likes for any user.
 */
@RestController
@RequiredArgsConstructor
public class InternalInteractionController implements InternalApi {

    private final SongInteractionService songInteractionService;

    @Override
    public ResponseEntity<UserLikesInternalResponse> getUserLikes(UUID userId, Integer page, Integer size) {
        Page<UUID> p = songInteractionService.getLikedSongs(userId, PageRequest.of(page, size));

        UserLikesInternalResponse response = new UserLikesInternalResponse()
                .songIds(p.getContent())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements((int) p.getTotalElements())
                .totalPages(p.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
