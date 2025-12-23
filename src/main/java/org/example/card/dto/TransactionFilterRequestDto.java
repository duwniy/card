package org.example.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.card.enums.Currency;
import org.example.card.enums.TransactionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionFilterRequestDto {

    private TransactionType type;
    private String transactionId;
    private String externalId;
    private Currency currency;
    private Integer page = 0;
    private Integer size = 10;

}
