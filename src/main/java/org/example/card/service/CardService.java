package org.example.card.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.example.card.dto.CardResponseDto;
import org.example.card.dto.CreateCardRequestDto;
import org.example.card.model.Card;
import org.example.card.model.IdempotencyRecord;
import org.example.card.enums.CardStatus;
import org.example.card.enums.Currency;
import org.example.card.exception.*;
import org.example.card.mapper.CardMapper;
import org.example.card.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Log4j2
public class CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public CardService(CardRepository cardRepository, CardMapper cardMapper, IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    // Создаем новую карту (с поддержкой idempotency)

    @Transactional
    public CardResponseDto createCard(CreateCardRequestDto request, String idempotencyKey, Long userId) {
        log.info("Creating card for user: {}, idempotency key: {}", userId, idempotencyKey);

        // Проверка idempotency
        Optional<IdempotencyRecord> existingRecord = idempotencyService.findByKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            log.info("Idempotency key already exists, returning cached response");
            try {
                return objectMapper.readValue(
                        existingRecord.get().getResponseBody(),
                        CardResponseDto.class
                );
            } catch (JsonProcessingException e) {
                log.error("Error deserializing cached response", e);
                throw new InvalidDataException("Error processing cached response");
            }
        }

        // Проверка лимита карт пользователя (максимум 3 не CLOSED карты)
        long activeCardsCount = cardRepository.countByUserIdAndStatusNot(userId, CardStatus.CLOSED);
        if (activeCardsCount >= 3) {
            throw new CardLimitExceededException("User already has 3 active cards");
        }

        // Валидация initial_amount
        if (request.getInitialAmount() != null && request.getInitialAmount() > 10000) {
            throw new InvalidDataException("initial_amount must not exceed 10000");
        }

        // Создание карты
        Card card = cardMapper.toCard(request);
        card.setUserId(userId);

        // Установка дефолтных значений
        if (card.getStatus() == null) {
            card.setStatus(CardStatus.ACTIVE);
        }
        if (card.getCurrency() == null) {
            card.setCurrency(Currency.UZS);
        }
        if (card.getBalance() == null) {
            card.setBalance(request.getInitialAmount() != null ? request.getInitialAmount() : 0L);
        }

        Card savedCard = cardRepository.save(card);
        CardResponseDto response = cardMapper.toDto(savedCard);

        // Сохранение idempotency записи
        idempotencyService.save(
                idempotencyKey,
                "POST /api/v1/cards",
                savedCard.getCardId(),
                "CARD",
                201,
                response
        );

        log.info("Card created successfully: {}", savedCard.getCardId());
        return response;
    }

    //Получить карту по ID
    @Transactional(readOnly = true)
    public CardResponseDto getCard(String cardId, Long userId) {
        log.info("Getting card: {} for user: {}", cardId, userId);

        Card card = cardRepository.findByCardId(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with such id not exists in processing."));

        // Проверка прав доступа
        if (!card.getUserId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to access this card");
        }

        return cardMapper.toDto(card);
    }

    // Заблокировать карту

    @Transactional
    public void blockCard(String cardId, String ifMatchHeader, Long userId) {
        log.info("Blocking card: {} for user: {}", cardId, userId);

        Card card = cardRepository.findByCardId(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with such id not exists in processing."));

        // Проверка прав доступа
        if (!card.getUserId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to block this card");
        }

        // Проверка ETag
        if (!String.valueOf(card.getVersion()).equals(ifMatchHeader.replace("\"", ""))) {
            throw new ETagMismatchException("Card data has been modified. Please refresh and try again.");
        }

        // Проверка статуса
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidCardStatusException("Card must be ACTIVE to be blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        log.info("Card blocked successfully: {}", cardId);
    }

    //Разблокировать карту
    @Transactional
    public void unblockCard(String cardId, String ifMatchHeader, Long userId) {
        log.info("Unblocking card: {} for user: {}", cardId, userId);

        Card card = cardRepository.findByCardId(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with such id not exists in processing."));

        // Проверка прав доступа
        if (!card.getUserId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to unblock this card");
        }

        // Проверка ETag
        if (!String.valueOf(card.getVersion()).equals(ifMatchHeader.replace("\"", ""))) {
            throw new ETagMismatchException("Card data has been modified. Please refresh and try again.");
        }

        // Проверка статуса
        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new InvalidCardStatusException("Card must be BLOCKED to be unblocked");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);

        log.info("Card unblocked successfully: {}", cardId);
    }

    //Получить версию карты для ETag
    @Transactional(readOnly = true)
    public Long getCardVersion(String cardId) {
        Card card = cardRepository.findByCardId(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with such id not exists in processing."));
        return card.getVersion();
    }
}