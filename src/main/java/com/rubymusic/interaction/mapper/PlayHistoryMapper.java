package com.rubymusic.interaction.mapper;

import com.rubymusic.interaction.dto.PlayHistoryResponse;
import com.rubymusic.interaction.model.PlayHistory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlayHistoryMapper {

    PlayHistoryResponse toDto(PlayHistory playHistory);

    List<PlayHistoryResponse> toDtoList(List<PlayHistory> history);
}
