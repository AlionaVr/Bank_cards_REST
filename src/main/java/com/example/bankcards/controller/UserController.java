package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.UserUpdateRequest;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "CRUD operations for users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a single user by their unique identifier. Available only for administrators.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User found successfully",
                            content = @Content(schema = @Schema(implementation = UserDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User ID") @PathVariable("id") UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all users",
            description = "Retrieves a paginated list of all users. Available only for administrators.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "List of users retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))
            )
    )
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @Parameter(description = "Page number (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of users per page") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field name") @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok(userService.getAllUsers(page, size, sortBy));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a user",
            description = "Deletes a user by their ID. Available only for administrators.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "user ID") @PathVariable("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user information",
            description = "Updates the user's details. Available only for administrators.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User updated successfully",
                            content = @Content(schema = @Schema(implementation = UserDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "User ID") @PathVariable("id") UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}
