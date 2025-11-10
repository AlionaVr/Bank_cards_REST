package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Registration and login")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Login user", description = "Login user with provided credentials, return JWT-token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful login",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid login or password")
            })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Attempt login for user '{}'", request.getLogin());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user", description = "Logout user",
            responses = {@ApiResponse(responseCode = "200", description = "Successful logout"),
                    @ApiResponse(responseCode = "401", description = "User is not logged in")
            })
    public ResponseEntity<Void> logout() {
        log.info("User is logging out");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registration of new user", description = "Create new user",
            responses = {@ApiResponse(responseCode = "200", description = "Successful registration"),
                    @ApiResponse(responseCode = "400", description = "Invalid registration data")
            })
    public ResponseEntity<UserDto> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
}
