package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        UserDto dto = userService.registerUser(request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/login/{login}")
    public ResponseEntity<UserDto> getUserByLogin(@Valid @PathVariable String login) {
        UserDto dto = userService.getUserByLogin(login);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(defaultValue = "createdAt") String sortBy) {
        Page<UserDto> users = userService.getAllUsers(page, size, sortBy);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestParam UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
    //TODO update user
}
