package com.example.bankcards.service;

import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwt;
    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .id(UUID.randomUUID())
                .login("johndoe")
                .passwordHash("hashed123")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("When credentials are valid, then return token")
    void login_WhenCredentialsAreValid_ThenReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setLogin("johndoe");
        request.setPassword("password123");

        when(userRepository.findByLogin("johndoe")).thenReturn(Optional.of(user));
        when(encoder.matches("password123", "hashed123")).thenReturn(true);
        when(jwt.generateAccessToken("johndoe", List.of("USER"))).thenReturn("fake-jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("fake-jwt-token", response.getAuthToken());
        verify(jwt).generateAccessToken("johndoe", List.of("USER"));
    }

    @Test
    @DisplayName("When user not found, then throw exception")
    void login_WhenUserNotFound_ThenThrowException() {
        LoginRequest request = new LoginRequest();
        request.setLogin("unknown");
        request.setPassword("password123");

        when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(request));
        verify(encoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("When password is invalid, then throw exception")
    void login_WhenPasswordInvalid_ThenThrowException() {
        LoginRequest request = new LoginRequest();
        request.setLogin("johndoe");
        request.setPassword("wrongPass");

        when(userRepository.findByLogin("johndoe")).thenReturn(Optional.of(user));
        when(encoder.matches("wrongPass", "hashed123")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        verify(jwt, never()).generateAccessToken(any(), any());
    }

    @Test
    @DisplayName("When login is not taken, then create user and return dto")
    void register_WhenLoginNotTaken_ThenCreateUserAndReturnDto() {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setLogin("newuser");
        req.setPassword("Password123");
        req.setEmail("new@bank.com");
        req.setFirstName("Alice");
        req.setLastName("Smith");

        when(userRepository.existsByLogin("newuser")).thenReturn(false);
        when(encoder.encode("Password123")).thenReturn("encodedPwd");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserDto result = authService.register(req);

        assertEquals("newuser", result.getLogin());
        assertEquals("new@bank.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("When login is taken, then throw exception")
    void register_WhenLoginExists_ThenThrowException() {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setLogin("existing");
        req.setPassword("Password123");
        req.setEmail("ex@bank.com");
        req.setFirstName("Eve");
        req.setLastName("Brown");

        when(userRepository.existsByLogin("existing")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any());
    }
}
