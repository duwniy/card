package org.example.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.card.enums.CardStatus;
import org.example.card.enums.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardResponseDto {

    @JsonProperty("card_id")
    private String cardId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("status")
    private CardStatus status;

    @JsonProperty("balance")
    private Long balance;

    @JsonProperty("currency")
    private Currency currency;
}
