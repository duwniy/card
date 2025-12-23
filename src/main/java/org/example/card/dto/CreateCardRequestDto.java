package org.example.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.card.enums.CardStatus;
import org.example.card.enums.Currency;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCardRequestDto {

    @NotNull(message = "user_id is required")
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("status")
    private CardStatus status;

    @Min(value = 0, message = "initial_amount must be positive")
    @Max(value = 10000, message = "initial_amount must be less than 10000")
    @JsonProperty("initial_amount")
    private Long initialAmount;

    @JsonProperty("currency")
    private Currency currency;
}
