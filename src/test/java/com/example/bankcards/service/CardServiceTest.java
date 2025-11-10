package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.CardCreationRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberMasker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardNumberMasker cardNumberMasker;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(UUID.randomUUID())
                .login("user1")
                .email("user1@test.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .build();

        card = Card.builder()
                .id(UUID.randomUUID())
                .cardHolderName("John Doe")
                .balance(BigDecimal.TEN)
                .owner(user)
                .status(CardStatus.ACTIVE)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getLogin(), "password")
        );

        when(userRepository.findByLogin(user.getLogin())).thenReturn(Optional.of(user));
        when(cardNumberMasker.maskCardNumber(anyString())).thenReturn("**** **** **** 1234");
    }

    @Test
    @DisplayName("Should create card with masked card number")
    void createCard_WhenUserExists_ThenCardCreatedSuccessfully() {
        CardCreationRequest req = new CardCreationRequest();
        req.setCardHolderName("John Doe");
        req.setInitialBalance(BigDecimal.valueOf(100));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardDto result = cardService.createCard(user.getId(), req);

        assertEquals("John Doe", result.getCardHolderName());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void createCard_WhenUserNotFound_ThenThrowException() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () ->
                cardService.createCard(UUID.randomUUID(), new CardCreationRequest()));
    }

    @Test
    @DisplayName("Should throw exception when card holder name is blank")
    void deleteCard_WhenBalanceZero_ThenDelete() {
        card.setBalance(BigDecimal.ZERO);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        cardService.deleteCard(card.getId());
        verify(cardRepository).delete(card);
    }

    @Test
    @DisplayName("Should throw exception when balance is not zero")
    void deleteCard_WhenBalanceNotZero_ThenThrowException() {
        card.setBalance(BigDecimal.valueOf(50));
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThrows(CardOperationException.class, () -> cardService.deleteCard(card.getId()));
        verify(cardRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when card not found")
    void activateCard_WhenValid_ThenSetActive() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        CardDto dto = cardService.activateCard(card.getId());
        assertEquals(CardStatus.ACTIVE, dto.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("Should throw exception when card not found")
    void activateCard_WhenExpired_ThenThrowException() {
        card.setStatus(CardStatus.EXPIRED);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThrows(CardOperationException.class, () -> cardService.activateCard(card.getId()));
    }

    @Test
    @DisplayName("Should throw exception when card not found")
    void blockCard_WhenActive_ThenStatusBlocked() {
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        CardDto dto = cardService.blockCard(card.getId());
        assertEquals(CardStatus.BLOCKED, dto.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when card not found")
    void blockCard_WhenExpired_ThenThrowException() {
        card.setStatus(CardStatus.EXPIRED);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThrows(CardOperationException.class, () -> cardService.blockCard(card.getId()));
    }

    @Test
    @DisplayName("Should throw exception when card not found")
    void requestBlockCard_WhenValid_ThenRequestSetTrue() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getLogin(), "password")
        );
        when(userRepository.findByLogin(user.getLogin())).thenReturn(Optional.of(user));
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        CardDto dto = cardService.requestBlockCard(card.getId());

        assertTrue(dto.isBlockRequested());
        verify(cardRepository).save(card);
    }


    @Test
    @DisplayName("Should throw exception when card not found")
    void requestBlockCard_WhenDifferentOwner_ThenAccessDenied() {
        User another = User.builder().id(UUID.randomUUID()).build();
        card.setOwner(another);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> cardService.requestBlockCard(card.getId()));
    }

    @Test
    @DisplayName("Should throw exception when card not found")
    void getBalance_WhenOwnerIsUser_ThenReturnBalance() {
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        BigDecimal result = cardService.getBalance(card.getId());
        assertEquals(BigDecimal.TEN, result);
    }

    @Test
    @DisplayName("Should throw exception when card not found")
    void getBalance_WhenDifferentOwner_ThenAccessDenied() {
        User other = User.builder().id(UUID.randomUUID()).build();
        card.setOwner(other);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> cardService.getBalance(card.getId()));
    }

    @Test
    @DisplayName("Should throw exception when card not found")
    void getAllCards_WhenCardsExist_ThenReturnDtoList() {
        when(cardRepository.findAll()).thenReturn(List.of(card));
        List<CardDto> result = cardService.getAllCards();

        assertEquals(1, result.size());
        verify(cardRepository).findAll();
    }

    @Test
    @DisplayName("Should throw exception when card not found")
    void getUserCards_WhenAdmin_ThenAllowAnyUser() {
        user.setRole(UserRole.ADMIN);
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findByOwner_Id(eq(user.getId()), any(Pageable.class))).thenReturn(page);

        Page<CardDto> result = cardService.getUserCards(user.getId(), null, null, 0, 5);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should throw exception when card not found")
    void getUserCards_WhenUserNotOwner_ThenAccessDenied() {
        UUID otherUserId = UUID.randomUUID();
        assertThrows(AccessDeniedException.class, () ->
                cardService.getUserCards(otherUserId, null, null, 0, 5));
    }
}
