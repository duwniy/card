package org.example.card.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.example.card.dto.CardResponseDto;
import org.example.card.dto.CreateCardRequestDto;
import org.example.card.security.UserPrincipal;
import org.example.card.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@Tag(name = "Card Management", description = "APIs for managing cards")
@Log4j2
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    @Operation(summary = "Create a new card", description = "Creates a new card for the authenticated user")
    public ResponseEntity<CardResponseDto> createCard(
            @Valid @RequestBody CreateCardRequestDto request,
            @RequestHeader("Idempotency-Key")
            @Parameter(description = "Unique request identifier for idempotency", required = true)
            String idempotencyKey,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        log.info("Creating card for user: {}, idempotency key: {}", userId, idempotencyKey);

        CardResponseDto response = cardService.createCard(request, idempotencyKey, userId);

        // Если карта уже была создана с этим idempotency key, возвращаем 200 OK
        // Иначе возвращаем 201 Created
        // Для простоты всегда возвращаем 201 (можно улучшить логику)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Get card by ID", description = "Retrieves card details by card ID")
    public ResponseEntity<CardResponseDto> getCard(
            @PathVariable
            @Parameter(description = "Card ID", required = true)
            String cardId,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        log.info("Getting card: {} for user: {}", cardId, userId);

        CardResponseDto response = cardService.getCard(cardId, userId);
        Long version = cardService.getCardVersion(cardId);

        // Добавляем ETag заголовок
        return ResponseEntity.ok()
                .eTag(String.valueOf(version))
                .body(response);
    }

    @PostMapping("/{cardId}/block")
    @Operation(summary = "Block card", description = "Blocks an active card")
    public ResponseEntity<Void> blockCard(
            @PathVariable
            @Parameter(description = "Card ID", required = true)
            String cardId,
            @RequestHeader("If-Match")
            @Parameter(description = "ETag value for optimistic locking", required = true)
            String ifMatch,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        log.info("Blocking card: {} for user: {}", cardId, userId);

        cardService.blockCard(cardId, ifMatch, userId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cardId}/unblock")
    @Operation(summary = "Unblock card", description = "Unblocks a blocked card")
    public ResponseEntity<Void> unblockCard(
            @PathVariable
            @Parameter(description = "Card ID", required = true)
            String cardId,
            @RequestHeader("If-Match")
            @Parameter(description = "ETag value for optimistic locking", required = true)
            String ifMatch,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        log.info("Unblocking card: {} for user: {}", cardId, userId);

        cardService.unblockCard(cardId, ifMatch, userId);

        return ResponseEntity.noContent().build();
    }
}