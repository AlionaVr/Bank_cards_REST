package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.dto.request.UserUpdateRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    private static final List<String> ALLOWED_SORT_FIELDS =
            List.of("createdAt", "email", "firstName", "lastName", "login");

    @Transactional
    public UserDto registerUser(UserRegistrationRequest request) {
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

        return convertUserToDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID userId) {
        log.info("Getting user with ID: {}", userId);
        User user = findUserById(userId);
        return convertUserToDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(int page, int size, String sortBy) {
        log.info("Getting all users - page: {}, size: {}, sortBy:{}", page, size, sortBy);

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "createdAt";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        return userRepository.findAll(pageable).map(this::convertUserToDto);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user with ID: {}", userId);

        User user = findUserById(userId);
        boolean hasCardsWithBalance = user.getCards().stream()
                .anyMatch(card -> card.getBalance().compareTo(BigDecimal.ZERO) > 0);

        if (hasCardsWithBalance) {
            throw new IllegalStateException("Cannot delete user with cards that have non-zero balance");
        }
        userRepository.deleteById(userId);
        log.info("User deleted successfully: {}", userId);
    }

    @Transactional
    public UserDto updateUser(UUID userId, UserUpdateRequest request) {
        User user = findUserById(userId);

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().toLowerCase().trim();
            if (!newEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new UserAlreadyExistsException("Email '" + newEmail + "' is already in use");
                }
                user.setEmail(newEmail);
            }
        }

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getRole() != null && request.getRole() != user.getRole()) {
            user.setRole(request.getRole());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);

        return convertUserToDto(updatedUser);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserDto convertUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}