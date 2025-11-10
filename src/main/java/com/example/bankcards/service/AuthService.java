package com.example.bankcards.service;

import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder;

    public LoginResponse login(LoginRequest request) {
        String login = request.getLogin();
        String rawPassword = request.getPassword();

        log.info("User '{}' is logging in", login);
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!encoder.matches(rawPassword, user.getPasswordHash())) {
            log.error("Bad credentials for user '{}'", login);
            throw new IllegalArgumentException("Bad credentials");
        }
        String token = jwt.generateAccessToken(user.getLogin(), List.of(user.getRole().name()));
        return new LoginResponse(token);
    }

    @Transactional
    public UserDto register(UserRegistrationRequest request) {
        log.info("Registering user with login: {}", request.getLogin());

        if (userRepository.existsByLogin(request.getLogin())) {
            throw new RuntimeException("User with login: " + request.getLogin() + " already exists");
        }

        String hashedPassword = encoder.encode(request.getPassword());

        User user = User.builder()
                .login(request.getLogin())
                .passwordHash(hashedPassword)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered with ID: {}", savedUser.getId());

        return new UserDto(savedUser);
    }
}
