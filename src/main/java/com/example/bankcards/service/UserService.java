package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.dto.request.UserUpdateRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public UserDto registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new RuntimeException("User with login: " + request.getLogin() + " already exists");
        }
        String hashedPassword = encoder.encode(request.getPassword());
        User user = User.builder().login(request.getLogin())
                .passwordHash(hashedPassword)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        User savedUser = userRepository.save(user);
        return convertUserToDto(savedUser);
    }

    public UserDto getUserByLogin(String login) {
        //TODO index on login in users?
        User user = userRepository.findByLogin(login.toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertUserToDto(user);
    }

    public Page<UserDto> getAllUsers(int page, int size, String sortBy) {

        List<String> allowedSortFields = List.of("createdAt", "email", "firstName", "lastName");
        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "createdAt";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        return userRepository.findAll(pageable).map(this::convertUserToDto);
    }

    public void deleteUser(UUID userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public UserDto updateUser(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail().trim());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        User updatedUser = userRepository.save(user);

        return convertUserToDto(updatedUser);
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