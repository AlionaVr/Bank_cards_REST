package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Page<Card> findByOwner_Id(UUID userId, Pageable pageable);

    Page<Card> findByOwner_IdAndStatus(UUID userId, CardStatus status, Pageable pageable);

    Page<Card> findByOwner_IdAndCardHolderNameContainingIgnoreCase(UUID userId, String search, Pageable pageable);

    Page<Card> findByOwner_IdAndStatusAndCardHolderNameContainingIgnoreCase(UUID userId, CardStatus status, String search, Pageable pageable);

}
