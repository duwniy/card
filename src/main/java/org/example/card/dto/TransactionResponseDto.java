package org.example.card.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.card.enums.Currency;
import org.example.card.enums.TransactionPurpose;
import org.example.card.enums.TransactionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponseDto {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("external_id")
    private String externalId;

    @JsonProperty("card_id")
    private String carId;

    @JsonProperty("amount")
    private Long amount;

    @JsonProperty("after_balance")
    private Long afterBalance;

    @JsonProperty("currency")
    private Currency currency;

    @JsonProperty("type")
    private TransactionType type;

    @JsonProperty("purpose")
    private TransactionPurpose purpose;

    @JsonProperty("exchange_rate")
    private Long exchangeRate;

}
