package org.example.card.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.example.card.dto.*;
import org.example.card.model.Card;
import org.example.card.model.IdempotencyRecord;
import org.example.card.model.Transaction;
import org.example.card.enums.CardStatus;
import org.example.card.enums.Currency;
import org.example.card.enums.TransactionType;
import org.example.card.exception.*;
import org.example.card.mapper.TransactionMapper;
import org.example.card.repository.CardRepository;
import org.example.card.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final TransactionMapper transactionMapper;
    private final ExchangeRateService exchangeRateService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public TransactionService(TransactionRepository transactionRepository, CardRepository cardRepository, TransactionMapper transactionMapper, ExchangeRateService exchangeRateService, IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.transactionMapper = transactionMapper;
        this.exchangeRateService = exchangeRateService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    //Списание средств (DEBIT)
    @Transactional
    public TransactionResponseDto debit(String cardId, DebitRequestDto request,
                                        String idempotencyKey, Long userId) {
        log.info("Processing debit for card: {}, amount: {}, idempotency key: {}",
                cardId, request.getAmount(), idempotencyKey);

        // Проверка idempotency
        Optional<IdempotencyRecord> existingRecord = idempotencyService.findByKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            log.info("Idempotency key already exists, returning cached response");
            try {
                return objectMapper.readValue(
                        existingRecord.get().getResponseBody(),
                        TransactionResponseDto.class
                );
            } catch (JsonProcessingException e) {
                log.error("Error deserializing cached response", e);
                throw new InvalidDataException("Error processing cached response");
            }
        }

        // Получить карту
        Card card = cardRepository.findByCardId(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with such id not exists in processing."));

        // Проверка прав доступа
        if (!card.getUserId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to perform this operation");
        }

        // Проверка статуса карты
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidCardStatusException("Card must be ACTIVE to perform transactions");
        }

        // Определить валюту транзакции
        Currency transactionCurrency = request.getCurrency() != null ?
                request.getCurrency() : Currency.UZS;

        // Конвертация суммы в валюту карты (если нужно)
        Long amountInCardCurrency = exchangeRateService.convert(
                request.getAmount(),
                transactionCurrency,
                card.getCurrency()
        );

        // Проверка баланса
        if (card.getBalance() < amountInCardCurrency) {
            throw new InsufficientFundsException("Insufficient funds on card");
        }

        // Списание
        card.setBalance(card.getBalance() - amountInCardCurrency);
        cardRepository.save(card);

        // Создание транзакции
        Transaction transaction = transactionMapper.toCard(request);
        transaction.setCardId(cardId);
        transaction.setType(TransactionType.DEBIT);
        transaction.setAfterBalance(card.getBalance());
        transaction.setCurrency(transactionCurrency);

        // Курс обмена (если была конвертация)
        if (transactionCurrency != card.getCurrency()) {
            transaction.setExchangeRate(
                    exchangeRateService.getExchangeRateInTiyin(transactionCurrency, card.getCurrency())
            );
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        TransactionResponseDto response = transactionMapper.toTransaction(savedTransaction);

        // Сохранение idempotency записи
        idempotencyService.save(
                idempotencyKey,
                "POST /api/v1/cards/" + cardId + "/debit",
                savedTransaction.getTransactionId(),
                "TRANSACTION",
                200,
                response
        );

        log.info("Debit transaction completed: {}", savedTransaction.getTransactionId());
        return response;
    }

    /**
     * Пополнение средств (CREDIT)
     */
    @Transactional
    public TransactionResponseDto credit(String cardId, CreditRequestDto request,
                                         String idempotencyKey, Long userId) {
        log.info("Processing credit for card: {}, amount: {}, idempotency key: {}",
                cardId, request.getAmount(), idempotencyKey);

        // Проверка idempotency
        Optional<IdempotencyRecord> existingRecord = idempotencyService.findByKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            log.info("Idempotency key already exists, returning cached response");
            try {
                return objectMapper.readValue(
                        existingRecord.get().getResponseBody(),
                        TransactionResponseDto.class
                );
            } catch (JsonProcessingException e) {
                log.error("Error deserializing cached response", e);
                throw new InvalidDataException("Error processing cached response");
            }
        }

        // Получить карту
        Card card = cardRepository.findByCardId(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with such id not exists in processing."));

        // Проверка прав доступа
        if (!card.getUserId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to perform this operation");
        }

        // Проверка статуса карты
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidCardStatusException("Card must be ACTIVE to perform transactions");
        }

        // Определить валюту транзакции
        Currency transactionCurrency = request.getCurrency() != null ?
                request.getCurrency() : Currency.UZS;

        // Конвертация суммы в валюту карты (если нужно)
        Long amountInCardCurrency = exchangeRateService.convert(
                request.getAmount(),
                transactionCurrency,
                card.getCurrency()
        );

        // Пополнение
        card.setBalance(card.getBalance() + amountInCardCurrency);
        cardRepository.save(card);

        // Создание транзакции
        Transaction transaction = transactionMapper.toCard(request);
        transaction.setCardId(cardId);
        transaction.setType(TransactionType.CREDIT);
        transaction.setAfterBalance(card.getBalance());
        transaction.setCurrency(transactionCurrency);

        // Курс обмена (если была конвертация)
        if (transactionCurrency != card.getCurrency()) {
            transaction.setExchangeRate(
                    exchangeRateService.getExchangeRateInTiyin(transactionCurrency, card.getCurrency())
            );
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        TransactionResponseDto response = transactionMapper.toTransaction(savedTransaction);

        // Сохранение idempotency записи
        idempotencyService.save(
                idempotencyKey,
                "POST /api/v1/cards/" + cardId + "/credit",
                savedTransaction.getTransactionId(),
                "TRANSACTION",
                200,
                response
        );

        log.info("Credit transaction completed: {}", savedTransaction.getTransactionId());
        return response;
    }

    // Получить историю транзакций с фильтрацией и пагинацией
    @Transactional(readOnly = true)
    public PageResponseDto<TransactionResponseDto> getTransactions(
            String cardId,
            TransactionFilterRequestDto filter,
            Long userId) {

        log.info("Getting transactions for card: {}, filters: {}", cardId, filter);

        // Проверка существования карты и прав доступа
        Card card = cardRepository.findByCardId(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with such id not exists in processing."));

        if (!card.getUserId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to view these transactions");
        }

        // Создание Pageable
        Pageable pageable = PageRequest.of(
                filter.getPage() != null ? filter.getPage() : 0,
                filter.getSize() != null ? filter.getSize() : 10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // Фильтрация
        Page<Transaction> transactionsPage = transactionRepository.findByFilters(
                cardId,
                filter.getType(),
                filter.getTransactionId(),
                filter.getExternalId(),
                filter.getCurrency(),
                pageable
        );

        // Маппинг в DTO
        List<TransactionResponseDto> content = transactionsPage.getContent()
                .stream()
                .map(transactionMapper::toTransaction)
                .collect(Collectors.toList());

        return PageResponseDto.<TransactionResponseDto>builder()
                .page(transactionsPage.getNumber())
                .size(transactionsPage.getSize())
                .totalPages(transactionsPage.getTotalPages())
                .totalItems(transactionsPage.getTotalElements())
                .content(content)
                .build();
    }
}