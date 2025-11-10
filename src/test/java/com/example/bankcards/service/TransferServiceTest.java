package com.example.bankcards.service;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    private TransferRepository transferRepository;
    private CardRepository cardRepository;
    private UserRepository userRepository;
    private TransferService transferService;

    private User user;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        transferRepository = mock(TransferRepository.class);
        cardRepository = mock(CardRepository.class);
        userRepository = mock(UserRepository.class);
        transferService = new TransferService(transferRepository, cardRepository, userRepository);

        user = User.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .role(UserRole.USER)
                .build();

        fromCard = Card.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(500))
                .owner(user)
                .status(com.example.bankcards.entity.CardStatus.ACTIVE)
                .build();

        toCard = Card.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(200))
                .owner(user)
                .status(com.example.bankcards.entity.CardStatus.ACTIVE)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(user.getLogin(), null)
        );
        when(userRepository.findByLogin(user.getLogin())).thenReturn(Optional.of(user));
    }

    private TransferRequest buildRequest(UUID fromId, UUID toId, BigDecimal amount, String desc) {
        TransferRequest req = new TransferRequest();
        req.setFromCardId(fromId);
        req.setToCardId(toId);
        req.setAmount(amount);
        req.setDescription(desc);
        return req;
    }

    @Test
    @DisplayName("Should return empty page when no transfers found")
    void transferBetweenCards_WhenValidRequest_ThenTransferAndSave() {
        TransferRequest req = buildRequest(fromCard.getId(), toCard.getId(), BigDecimal.valueOf(100), "Test transfer");

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        Transfer saved = Transfer.builder()
                .id(UUID.randomUUID())
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(req.getAmount())
                .description(req.getDescription())
                .transferDate(LocalDateTime.now())
                .build();

        when(transferRepository.save(any(Transfer.class))).thenReturn(saved);

        TransferDto dto = transferService.transferBetweenCards(req);

        assertNotNull(dto);
        assertEquals(req.getAmount(), dto.getAmount());
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    @DisplayName("Should return empty page when no transfers found")
    void transferBetweenCards_WhenSameCard_ThenThrowCardOperationException() {
        TransferRequest req = buildRequest(fromCard.getId(), fromCard.getId(), BigDecimal.valueOf(50), "Invalid");

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));

        assertThrows(CardOperationException.class, () -> transferService.transferBetweenCards(req));
    }

    @Test
    @DisplayName("Should return empty page when no transfers found")
    void transferBetweenCards_WhenCardNotOwnedByUser_ThenThrowAccessDeniedException() {
        User other = User.builder().id(UUID.randomUUID()).login("other").build();
        Card otherCard = Card.builder()
                .id(UUID.randomUUID())
                .owner(other)
                .balance(BigDecimal.valueOf(1000))
                .status(com.example.bankcards.entity.CardStatus.ACTIVE)
                .build();

        TransferRequest req = buildRequest(fromCard.getId(), otherCard.getId(), BigDecimal.valueOf(10), "Unauthorized");

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(otherCard.getId())).thenReturn(Optional.of(otherCard));

        assertThrows(AccessDeniedException.class, () -> transferService.transferBetweenCards(req));
    }

    @Test
    @DisplayName("Should return empty page when no transfers found")
    void transferBetweenCards_WhenCardNotActive_ThenThrowCardOperationException() {
        fromCard.setStatus(com.example.bankcards.entity.CardStatus.BLOCKED);

        TransferRequest req = buildRequest(fromCard.getId(), toCard.getId(), BigDecimal.TEN, "Inactive");

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThrows(CardOperationException.class, () -> transferService.transferBetweenCards(req));
    }

    @Test
    @DisplayName("Should return empty page when no transfers found")
    void transferBetweenCards_WhenInsufficientFunds_ThenThrowCardOperationException() {
        TransferRequest req = buildRequest(fromCard.getId(), toCard.getId(), BigDecimal.valueOf(600), "Too much");

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThrows(CardOperationException.class, () -> transferService.transferBetweenCards(req));
    }

    @Test
    @DisplayName("Should return empty page when no transfers found")
    void transferBetweenCards_WhenCardNotFound_ThenThrowCardNotFoundException() {
        TransferRequest req = buildRequest(fromCard.getId(), toCard.getId(), BigDecimal.TEN, "Missing card");

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> transferService.transferBetweenCards(req));
    }

    @Test
    @DisplayName("Should return empty page when no transfers found")
    void getTransferHistory_WhenCardNotOwnedByUser_ThenThrowAccessDenied() {
        User other = User.builder().id(UUID.randomUUID()).login("other").build();
        Card otherCard = Card.builder()
                .id(UUID.randomUUID())
                .owner(other)
                .status(com.example.bankcards.entity.CardStatus.ACTIVE)
                .balance(BigDecimal.TEN)
                .build();

        when(cardRepository.findById(otherCard.getId())).thenReturn(Optional.of(otherCard));

        assertThrows(AccessDeniedException.class,
                () -> transferService.getTransferHistory(0, 5, otherCard.getId()));
    }

    @Test
    @DisplayName("Should return empty page when no transfers found")
    void getTransferHistory_WhenUserNotFound_ThenThrowUserNotFoundException() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("ghost", null)
        );
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> transferService.getTransferHistory(0, 5, null));
    }
}
