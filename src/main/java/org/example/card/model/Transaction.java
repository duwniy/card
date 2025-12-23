package org.example.card.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.card.enums.Currency;
import org.example.card.enums.TransactionPurpose;
import org.example.card.enums.TransactionType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_card_id", columnList = "card_id"),
        @Index(name = "idx_external_id", columnList = "external_id"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private String transactionId;

    @Column(name = "external_id", unique = true, nullable = false)
    private String externalId;

    @Column(name = "card_id", nullable = false)
    private String cardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TransactionType type;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "after_balance", nullable = false)
    private Long afterBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 20)
    private TransactionPurpose purpose; // esli DEBIT

    @Column(name = "exchange_rate")
    private Long exchangeRate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if(transactionId == null) {
            transactionId = UUID.randomUUID().toString();
        }
    }
}
