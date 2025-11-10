package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Operations for transferring money between cards")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Transfer money between cards",
            description = "Transfer money from one card to another. Users can only transfer between their own cards.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transfer completed successfully",
                            content = @Content(schema = @Schema(implementation = TransferDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid transfer request"),
                    @ApiResponse(responseCode = "403", description = "Access denied - not owner of cards"),
                    @ApiResponse(responseCode = "404", description = "Card not found")
            }
    )
    public ResponseEntity<TransferDto> transferMoneyBetweenUserCards(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transferService.transferBetweenCards(request));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Get transfer history",
            description = "Retrieves paginated transfer history for the current user. Admins can see all transfers.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transfer history retrieved successfully",
                            content = @Content(schema = @Schema(implementation = TransferDto.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Card not found")}
    )

    public ResponseEntity<Page<TransferDto>> getTransferHistory(
            @Parameter(description = "Page number (0..N)") @RequestParam(defaultValue = "0") @Valid int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Valid int size,
            @Parameter(description = "Filter by card ID") @RequestParam(required = false) UUID cardId) {
        return ResponseEntity.ok(transferService.getTransferHistory(page, size, cardId));
    }
}
