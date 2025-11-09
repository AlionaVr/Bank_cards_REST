package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Attempt login for user '{}'", request.getLogin());
        return ResponseEntity.ok(authService.login(request.getLogin(), request.getPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        log.info("User is logging out");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
}
