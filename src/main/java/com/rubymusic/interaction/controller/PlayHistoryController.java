package com.rubymusic.interaction.controller;

import com.rubymusic.interaction.dto.PlayHistoryPage;
import com.rubymusic.interaction.dto.RecordPlayRequest;
import com.rubymusic.interaction.mapper.PlayHistoryMapper;
import com.rubymusic.interaction.service.PlayHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlayHistoryController implements PlayHistoryApi {

    private final PlayHistoryService playHistoryService;
    private final PlayHistoryMapper playHistoryMapper;
    private final HttpServletRequest httpRequest;

    @Override
    public Optional<HttpServletRequest> getRequest() {
        return Optional.of(httpRequest);
    }

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
}
