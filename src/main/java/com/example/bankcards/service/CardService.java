package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreationRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public void createCard(Long userId, CardCreationRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Card card = Card.builder()
                .cardNumber(generateCardNumber())
                .cardHolderName(request.getCardHolderName())
                .balance(request.getInitialBalance())
                .owner(user)
                .build();

        cardRepository.save(card);
    }

    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));
        if (card.isActive() && card.isNotExpired()) {
            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);
        }
    }

    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));
        if (card.isBlocked() && card.isNotExpired()) {
            card.setStatus(CardStatus.ACTIVE);
            cardRepository.save(card);
        }
    }

    public void deleteCard(Long cardId) {
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

    public List<Card> getUserCards(Long userId) {
        return cardRepository.findByOwner(userId);
    }

    private String generateCardNumber(){
        Random randomNumber = new Random();
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i<16; i++){
            cardNumber.append(randomNumber.nextInt(10));
        }
        return cardNumber.toString();
    }
}