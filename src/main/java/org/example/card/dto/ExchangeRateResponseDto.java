package org.example.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateResponseDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("Ccy")
    private String ccy;

    @JsonProperty("CcyNm_RU")
    private String ccyNmRu;

    @JsonProperty("Nominal")
    private String nominal;

    @JsonProperty("Rate")
    private String rate;

    @JsonProperty("Diff")
    private String diff;

    @JsonProperty("Date")
    private String date;
}