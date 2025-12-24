package org.example.card.repository;

import org.example.card.enums.CardStatus;
import org.example.card.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    Optional<Card> findByCardId(String id);
    @Query("SELECT COUNT(c) FROM Card c WHERE c.userId = :userId AND c.status != :status")
    long countByUserIdAndStatusNot(@Param("userId") Long userId, @Param("status") CardStatus status);
    boolean existsByCardId(String cardId);

}
