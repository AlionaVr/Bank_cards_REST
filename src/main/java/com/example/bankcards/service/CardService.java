package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.CardCreationRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private static final AtomicInteger counter = new AtomicInteger(0);
    private final CardNumberMasker cardNumberMasker;

    @Transactional
    public CardDto createCard(UUID userId, CardCreationRequest request) {
        log.info("Creating card for user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Card card = Card.builder()
                .cardNumber(generateUniqueCardNumber())
                .cardHolderName(request.getCardHolderName())
                .balance(request.getInitialBalance())
                .owner(user)
                .build();

        cardRepository.save(card);
        log.info("Card created with ID: {}", card.getId());

        return convertCardToDto(card);
    }

    @Transactional
    public CardDto blockCard(UUID cardId) {
        log.info("Blocking card with ID: {}", cardId);
        Card card = findCardById(cardId);

        if (!card.isActive() || card.isExpired()) {
            throw new CardOperationException("Cannot block card. Card must be ACTIVE and not expired. Current status: " + card.getStatus());
        }
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        log.info("Card blocked with ID: {}", card.getId());

        return convertCardToDto(card);
    }

    @Transactional
    public CardDto activateCard(UUID cardId) {
        Card card = findCardById(cardId);

        if (card.isBlocked() || card.isExpired()) {
            throw new CardOperationException("Cannot activate card. Card must be BLOCKED and not expired. Current status: " + card.getStatus());
        }
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);

        log.info("Card activated with ID: {}", card.getId());
        return convertCardToDto(card);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        log.info("Deleting card with ID: {}", cardId);

        Card card = findCardById(cardId);

        if (card.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new CardOperationException("Cannot delete card with non-zero balance");
        }
        cardRepository.delete(card);
        log.info("Card deleted with ID: {}", cardId);
    }

    private Card findCardById(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    @Transactional(readOnly = true)
    public List<CardDto> getAllCards() {
        log.info("Getting all cards");

        return cardRepository.findAll()
                .stream()
                .map(this::convertCardToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CardDto> getUserCards(UUID userId, CardStatus status, int page, int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();

        User currentUser = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + login));

        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        if (!isAdmin && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own cards");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Card> cards = (status == null)
                ? cardRepository.findByOwner_Id(userId, pageable)
                : cardRepository.findByOwner_IdAndStatus(userId, status, pageable);

        return cards.map(this::convertCardToDto);
    }

    private String generateUniqueCardNumber() {

        Instant now = Instant.now();
        long milliseconds = now.toEpochMilli();

        int count = counter.updateAndGet(i -> (i + 1) % 10);
        return String.format("%015d", milliseconds) + count;

    }

    private CardDto convertCardToDto(Card card) {
        return CardDto.builder()
                .id(card.getId())
                .cardHolderName(card.getCardHolderName())
                .maskedCardNumber(cardNumberMasker.maskCardNumber(card.getCardNumber()))
                .ownerId(card.getOwner().getId())
                .createdDate(card.getCreatedDate())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .build();
    }
}