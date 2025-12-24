package org.example.card.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${cbu.api.base-url:https://cbu.uz/ru/arkhiv-kursov-valyut/json}")
    private String cbuApiBaseUrl;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(cbuApiBaseUrl)
                .build();
    }
}