package com.rubymusic.interaction.controller;

import com.rubymusic.interaction.dto.PlayHistoryPage;
import com.rubymusic.interaction.dto.PlaybackCheckpointRequest;
import com.rubymusic.interaction.dto.PlaybackCheckpointResponse;
import com.rubymusic.interaction.dto.RecordPlayRequest;
import com.rubymusic.interaction.mapper.PlayHistoryMapper;
import com.rubymusic.interaction.service.PlayHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlayHistoryController implements PlayHistoryApi {

    private final PlayHistoryService playHistoryService;
    private final PlayHistoryMapper playHistoryMapper;
    private final HttpServletRequest httpRequest;

    private UUID currentUserId() {
        return UUID.fromString(httpRequest.getHeader("X-User-Id"));
    }

    @Override
    public ResponseEntity<PlayHistoryPage> getPlayHistory(Integer page, Integer size) {
        var p = playHistoryService.getPlayHistory(currentUserId(), PageRequest.of(page, size));
        PlayHistoryPage dto = new PlayHistoryPage()
                .content(playHistoryMapper.toDtoList(p.getContent()))
                .totalElements((int) p.getTotalElements())
                .totalPages(p.getTotalPages())
                .page(p.getNumber())
                .size(p.getSize());
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<Void> recordPlay(RecordPlayRequest body) {
        playHistoryService.recordPlay(currentUserId(), body.getSongId(), body.getDurationPlayedSeconds());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PlaybackCheckpointResponse> getPlaybackCheckpoint() {
        return playHistoryService.getPlaybackCheckpoint(currentUserId())
                .map(checkpoint -> ResponseEntity.ok(new PlaybackCheckpointResponse()
                        .songId(checkpoint.getSongId())
                        .currentTimeSeconds(checkpoint.getCurrentTimeSeconds())
                        .updatedAt(checkpoint.getUpdatedAt())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    @Override
    public ResponseEntity<Void> upsertPlaybackCheckpoint(PlaybackCheckpointRequest body) {
        playHistoryService.savePlaybackCheckpoint(currentUserId(), body.getSongId(), body.getCurrentTimeSeconds());
        return ResponseEntity.noContent().build();
    }
}
