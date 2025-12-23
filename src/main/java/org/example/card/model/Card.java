package org.example.card.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.card.enums.CardStatus;
import org.example.card.enums.Currency;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cards", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Card {
    @Id
    @Column(name = "card_id", updatable = false, nullable = false)
    private String cardId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CardStatus status;

    @Column(name = "balance", nullable = false)
    private Long balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updateAt;

    public void prePersist() {
        if (cardId == null) {
            cardId = UUID.randomUUID().toString();
        }
        if (status == null) {
            status = CardStatus.ACTIVE;
        }
        if (balance == null) {
            balance = 0l;
        }
        if (currency == null) {
            currency = Currency.UZS;
        }

    }
}
