package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.UserUpdateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private BCryptPasswordEncoder encoder;
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        encoder = mock(BCryptPasswordEncoder.class);
        userService = new UserService(userRepository, encoder);

        user = User.builder()
                .id(UUID.randomUUID())
                .login("jdoe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .cards(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("When user exists, then return user dto")
    void getUserById_WhenUserExists_ThenReturnUserDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto dto = userService.getUserById(user.getId());

        assertEquals(user.getEmail(), dto.getEmail());
        verify(userRepository).findById(user.getId());
    }

    @Test
    @DisplayName("When user not found, then throw exception")
    void getUserById_WhenUserNotFound_ThenThrowException() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Should return page of users")
    void getAllUsers_WhenValidSortProvided_ThenReturnPagedUsers() {
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Page<UserDto> result = userService.getAllUsers(0, 10, "email");

        assertEquals(1, result.getTotalElements());
        assertEquals("john@example.com", result.getContent().get(0).getEmail());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(captor.capture());
        assertEquals(Sort.Direction.DESC,
                captor.getValue().getSort().getOrderFor("email").getDirection());
    }

    @Test
    @DisplayName("Should fallback to createdAt when invalid sort provided")
    void getAllUsers_WhenInvalidSortProvided_ThenFallbackToCreatedAt() {
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        userService.getAllUsers(0, 5, "bad_field");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(captor.capture());
        assertNotNull(captor.getValue().getSort().getOrderFor("createdAt"));
    }

    @Test
    @DisplayName("Should return empty page when no users found")
    void deleteUser_WhenNoCardsWithBalance_ThenDeleteSuccessfully() {
        user.setCards(List.of(Card.builder().balance(BigDecimal.ZERO).build()));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.deleteUser(user.getId());

        verify(userRepository).deleteById(user.getId());
    }

    @Test
    @DisplayName("Should throw exception when card with negative balance exists")
    void deleteUser_WhenCardWithPositiveBalanceExists_ThenThrowException() {
        user.setCards(List.of(Card.builder().balance(BigDecimal.valueOf(100)).build()));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class,
                () -> userService.deleteUser(user.getId()));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void updateUser_WhenValidDataProvided_ThenUpdateUserFields() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserUpdateRequest req = new UserUpdateRequest();
        req.setEmail("new@example.com");
        req.setFirstName("New");
        req.setLastName("Name");
        req.setRole(UserRole.ADMIN);

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        UserDto dto = userService.updateUser(user.getId(), req);

        assertEquals("new@example.com", dto.getEmail());
        assertEquals("New", dto.getFirstName());
        assertEquals(UserRole.ADMIN, dto.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void updateUser_WhenEmailAlreadyExists_ThenThrowException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

        UserUpdateRequest req = new UserUpdateRequest();
        req.setEmail("exists@example.com");

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.updateUser(user.getId(), req));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should keep original values when no fields provided")
    void updateUser_WhenFieldsAreBlankOrNull_ThenKeepOriginalValues() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserUpdateRequest req = new UserUpdateRequest();
        req.setEmail(" ");
        req.setFirstName(null);
        req.setLastName("");
        req.setRole(null);

        UserDto dto = userService.updateUser(user.getId(), req);

        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getFirstName(), dto.getFirstName());
        assertEquals(user.getLastName(), dto.getLastName());
        assertEquals(user.getRole(), dto.getRole());
    }
}
