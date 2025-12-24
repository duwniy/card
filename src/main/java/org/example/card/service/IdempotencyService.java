package org.example.card.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.example.card.model.IdempotencyRecord;
import org.example.card.repository.IdempotencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Log4j2
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRepository idempotencyRepository, ObjectMapper objectMapper) {
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
    }

    // Проверяем существование idempotency key
    public Optional<IdempotencyRecord> findByKey(String idempotencyKey) {
        return idempotencyRepository.findByIdempotencyKey(idempotencyKey);
    }

    // Сохраняем запись об операции
    @Transactional
    public void save(String idempotencyKey, String endpoint, String resourceId,
                     String resourceType, Integer responseStatus, Object responseBody) {
        try {
            String responseBodyJson = objectMapper.writeValueAsString(responseBody);

            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .endpoint(endpoint)
                    .resourceId(resourceId)
                    .resourceType(resourceType)
                    .responseStatus(responseStatus)
                    .responseBody(responseBodyJson)
                    .expiredAt(LocalDateTime.now().plusHours(24))
                    .build();

            idempotencyRepository.save(record);
            log.info("Saved idempotency record for key: {}", idempotencyKey);

        } catch (JsonProcessingException e) {
            log.error("Error serializing response body: {}", e.getMessage());
        }
    }

    //Очистка устаревших записей
    @Transactional
    public void cleanupExpiredRecords() {
        idempotencyRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired idempotency records");
    }
}