package org.example.card.mapper;

import org.example.card.dto.CreditRequestDto;
import org.example.card.dto.DebitRequestDto;
import org.example.card.dto.TransactionResponseDto;
import org.example.card.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionResponseDto toTransaction(Transaction transaction);

    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "cardId", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "afterBalance", ignore = true)
    @Mapping(target = "exchangeRate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Transaction toCard(DebitRequestDto dto);

    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "cardId", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "afterBalance", ignore = true)
    @Mapping(target = "purpose", ignore = true)
    @Mapping(target = "exchangeRate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Transaction toCard(CreditRequestDto dto);
}
