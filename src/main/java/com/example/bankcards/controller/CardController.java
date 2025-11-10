package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.CardCreationRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Operations for managing bank cards")
@SecurityRequirement(name = "bearerAuth")
public class CardController {
    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new card for a user",
            description = "Creates a new bank card and assigns it to a specific user. Available only for administrators.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Card created successfully",
                            content = @Content(schema = @Schema(implementation = CardDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    public ResponseEntity<CardDto> createCard(
            @RequestBody CardCreationRequest request,
            @Parameter(description = "User ID to assign the card to") @RequestParam UUID userId) {
        return ResponseEntity.ok(cardService.createCard(userId, request));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a card",
            description = "Removes a card from the system by its ID. Available only for administrators.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Card not found")
            }
    )
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "Card ID") @RequestParam UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/activate/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Activate a card",
            description = "Changes the card status to ACTIVE. Available only for administrators.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Card activated successfully",
                            content = @Content(schema = @Schema(implementation = CardDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Card not found")
            }
    )
    public ResponseEntity<CardDto> activateCard(
            @Parameter(description = "Card ID") @PathVariable("cardId") UUID cardId) {
        return ResponseEntity.ok(cardService.activateCard(cardId));
    }

    @PutMapping("/block/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Block a card",
            description = "Changes the card status to BLOCKED. Available only for administrators.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Card blocked successfully",
                            content = @Content(schema = @Schema(implementation = CardDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Card not found")
            }
    )
    public ResponseEntity<CardDto> blockCard(
            @Parameter(description = "Card ID") @PathVariable("cardId") UUID cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(
            summary = "Get cards of a specific user",
            description = "Retrieves a paginated list of all cards belonging to a specific user. " +
                    "Users can view only their own cards. Supports filtering by status and search. ",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of user's cards",
                            content = @Content(schema = @Schema(implementation = CardDto.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<Page<CardDto>> getUserCards(
            @Parameter(description = "User ID") @PathVariable("userId") UUID userId,
            @Parameter(description = "Page number (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by card status") @RequestParam(required = false) CardStatus status,
            @Parameter(description = "Search term (card holder name)") @RequestParam(required = false) String search) {

        Page<CardDto> dtoPage = cardService.getUserCards(userId, status, search, page, size);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all cards",
            description = "Retrieves a list of all cards in the system. Available only for administrators.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "List of all cards",
                    content = @Content(schema = @Schema(implementation = CardDto.class))
            )
    )
    public ResponseEntity<List<CardDto>> getAllCards() {
        List<CardDto> listDto = cardService.getAllCards();
        return ResponseEntity.ok(listDto);
    }

    @PutMapping("/request-block/{cardId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Request card blocking",
            description = "User can request to block their own card.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Card blocked successfully",
                            content = @Content(schema = @Schema(implementation = CardDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Card not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied - not card owner")
            }
    )
    public ResponseEntity<CardDto> requestBlockCard(
            @Parameter(description = "Card ID") @PathVariable("cardId") UUID cardId) {
        return ResponseEntity.ok(cardService.requestBlockCard(cardId));
    }

    @GetMapping("/balance/{cardId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Get card balance",
            description = "Retrieves the balance of a specific card. Users can only view their own card balance.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Balance retrieved successfully",
                            content = @Content(schema = @Schema(implementation = BigDecimal.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Card not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<BigDecimal> getBalance(
            @Parameter(description = "Card ID") @PathVariable("cardId") UUID cardId) {
        return ResponseEntity.ok(cardService.getBalance(cardId));
    }
}
