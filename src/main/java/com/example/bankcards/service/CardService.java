package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreationRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private static int counter = 0;

    public void createCard(UUID userId, CardCreationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Card card = Card.builder()
                .cardNumber(generateUniqueCardNumber())
                .cardHolderName(request.getCardHolderName())
                .balance(request.getInitialBalance())
                .owner(user)
                .build();

        cardRepository.save(card);
    }

    public void blockCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));
        if (card.isActive() && card.isNotExpired()) {
            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);
        }
    }

    public void activateCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));
        if (card.isBlocked() && card.isNotExpired()) {
            card.setStatus(CardStatus.ACTIVE);
            cardRepository.save(card);
        }
    }

    public void deleteCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        if (card.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot delete card with non-zero balance");
        }
        cardRepository.delete(card);
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Page<Card> getUserCards(UUID userId, CardStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status == null) {
            return cardRepository.findByOwner_Id(userId, pageable);
        } else {
            return cardRepository.findByOwner_IdAndStatus(userId, status, pageable);
        }
    }

    private String generateUniqueCardNumber() {

        Instant now = Instant.now();
        long milliseconds = now.toEpochMilli();

        synchronized (CardService.class) {
            counter = (counter + 1) % 10;
            return String.format("%015d", milliseconds) + counter;
        }
    }
}