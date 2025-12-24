package org.example.card.repository;

import org.example.card.enums.Currency;
import org.example.card.enums.TransactionType;
import org.example.card.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByExternalId(String cardId, Pageable pageable);
    Page<Transaction> findByCardId(String cardId, Pageable pageable);

    // Filtration
    @Query("SELECT t FROM Transaction t WHERE t.cardId = :cardId " +
            "AND (:type IS NULL OR t.type = :type) " +
            "AND (:transactionId IS NULL OR t.transactionId = :transactionId) " +
            "AND (:externalId IS NULL OR t.externalId = :externalId) " +
            "AND (:currency IS NULL OR t.currency = :currency)")
    Page<Transaction> findByFilters(
            @Param("cardId") String cardId,
            @Param("type") TransactionType type,
            @Param("transactionId") String transactionId,
            @Param("externalId") String externalId,
            @Param("currency") Currency currency,
            Pageable pageable
    );

    boolean existsByExternalId(String externalId);
}
