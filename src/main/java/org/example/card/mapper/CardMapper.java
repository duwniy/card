package org.example.card.mapper;

import org.example.card.dto.CardResponseDto;
import org.example.card.dto.CreateCardRequestDto;
import org.example.card.model.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardResponseDto toDto(Card card);

    Card toCard(CreateCardRequestDto dto);

    @Mapping(target = "cardId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    void updateCard(CreateCardRequestDto dto, @MappingTarget Card card);

}
