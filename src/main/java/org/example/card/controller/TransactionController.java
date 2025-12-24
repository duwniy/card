package org.example.card.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.example.card.dto.*;
import org.example.card.security.UserPrincipal;
import org.example.card.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards/{cardId}")
@Tag(name = "Transaction Management", description = "APIs for card transactions")
@Log4j2
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/debit")
    @Operation(summary = "Withdraw funds", description = "Withdraws funds from the card")
    public ResponseEntity<TransactionResponseDto> debit(
            @PathVariable
            @Parameter(description = "Card ID", required = true)
            String cardId,
            @Valid @RequestBody DebitRequestDto request,
            @RequestHeader("Idempotency-Key")
            @Parameter(description = "Unique request identifier for idempotency", required = true)
            String idempotencyKey,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        log.info("Processing debit for card: {}, amount: {}, user: {}",
                cardId, request.getAmount(), userId);

        TransactionResponseDto response = transactionService.debit(cardId, request, idempotencyKey, userId);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/credit")
    @Operation(summary = "Top up funds", description = "Adds funds to the card")
    public ResponseEntity<TransactionResponseDto> credit(
            @PathVariable
            @Parameter(description = "Card ID", required = true)
            String cardId,
            @Valid @RequestBody CreditRequestDto request,
            @RequestHeader("Idempotency-Key")
            @Parameter(description = "Unique request identifier for idempotency", required = true)
            String idempotencyKey,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        log.info("Processing credit for card: {}, amount: {}, user: {}",
                cardId, request.getAmount(), userId);

        TransactionResponseDto response = transactionService.credit(cardId, request, idempotencyKey, userId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/transactions")
    @Operation(summary = "Get transaction history", description = "Retrieves transaction history with filters and pagination")
    public ResponseEntity<PageResponseDto<TransactionResponseDto>> getTransactions(
            @PathVariable
            @Parameter(description = "Card ID", required = true)
            String cardId,
            @RequestParam(required = false)
            @Parameter(description = "Transaction type filter (DEBIT or CREDIT)")
            String type,
            @RequestParam(required = false)
            @Parameter(description = "Transaction ID filter")
            String transactionId,
            @RequestParam(required = false)
            @Parameter(description = "External ID filter")
            String externalId,
            @RequestParam(required = false)
            @Parameter(description = "Currency filter (UZS or USD)")
            String currency,
            @RequestParam(required = false, defaultValue = "0")
            @Parameter(description = "Page number (0-based)")
            Integer page,
            @RequestParam(required = false, defaultValue = "10")
            @Parameter(description = "Page size")
            Integer size,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        log.info("Getting transactions for card: {}, user: {}", cardId, userId);

        TransactionFilterRequestDto filter = TransactionFilterRequestDto.builder()
                .type(type != null ? org.example.card.enums.TransactionType.valueOf(type) : null)
                .transactionId(transactionId)
                .externalId(externalId)
                .currency(currency != null ? org.example.card.enums.Currency.valueOf(currency) : null)
                .page(page)
                .size(size)
                .build();

        PageResponseDto<TransactionResponseDto> response =
                transactionService.getTransactions(cardId, filter, userId);

        return ResponseEntity.ok(response);
    }
}