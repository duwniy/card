package org.example.card.service;

import lombok.extern.log4j.Log4j2;
import org.example.card.dto.ExchangeRateResponseDto;
import org.example.card.enums.Currency;
import org.example.card.exception.ExchangeRateException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Log4j2
public class ExchangeRateService {

    private final WebClient webClient;

    public ExchangeRateService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Получить курс USD к UZS от CBU
     * Результат кешируется на 1 час
     **/
    @Cacheable(value = "exchangeRates", key = "'USD_TO_UZS'")
    public BigDecimal getUsdToUzsRate() {
        try {
            log.info("Fetching USD to UZS exchange rate from CBU API");

            ExchangeRateResponseDto[] response = webClient.get()
                    .uri("/USD/")
                    .retrieve()
                    .bodyToMono(ExchangeRateResponseDto[].class)
                    .block();

            if (response == null || response.length == 0) {
                throw new ExchangeRateException("No exchange rate data received from CBU");
            }

            String rateStr = response[0].getRate();
            BigDecimal rate = new BigDecimal(rateStr);

            log.info("USD to UZS rate: {}", rate);
            return rate;

        } catch (Exception e) {
            log.error("Error fetching exchange rate from CBU: {}", e.getMessage());
            throw new ExchangeRateException("Failed to fetch exchange rate", e);
        }
    }

    /**
     * Конвертировать сумму из одной валюты в другую
     * @param amount сумма в тийинах
     * @param fromCurrency исходная валюта
     * @param toCurrency целевая валюта
     * @return сумма в тиинах целевой валюты
     */
    public Long convert(Long amount, Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency == toCurrency) {
            return amount;
        }

        BigDecimal rate = getUsdToUzsRate();

        // USD -> UZS
        if (fromCurrency == Currency.USD && toCurrency == Currency.UZS) {
            BigDecimal rateInTiyin = rate.multiply(BigDecimal.valueOf(100));
            return BigDecimal.valueOf(amount)
                    .multiply(rateInTiyin)
                    .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)
                    .longValue();
        }

        // UZS -> USD
        if (fromCurrency == Currency.UZS && toCurrency == Currency.USD) {
            BigDecimal rateInTiyin = rate.multiply(BigDecimal.valueOf(100));
            return BigDecimal.valueOf(amount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(rateInTiyin, RoundingMode.HALF_UP)
                    .longValue();
        }

        throw new ExchangeRateException("Unsupported currency conversion");
    }

    // Получить курс обмена в тийинах для response

    public Long getExchangeRateInTiyin(Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency == toCurrency) {
            return null;
        }

        BigDecimal rate = getUsdToUzsRate();
        return rate.multiply(BigDecimal.valueOf(100)).longValue();
    }
}