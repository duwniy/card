package org.example.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;
import org.example.card.enums.Currency;
import org.example.card.enums.TransactionPurpose;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebitRequestDto {
    @NotBlank(message = "external_id is required")
    @JsonProperty("external_id")
    private String externalId;

    @NotNull(message = "amount is required")
    @Min(value = 1, message = "amount must be positive")
    @JsonProperty("amount")
    private Long amount;

    @JsonProperty("currency")
    private Currency currency;

    @NotNull(message = "purpose is required")
    @JsonProperty("purpose")
    private TransactionPurpose purpose;

}
