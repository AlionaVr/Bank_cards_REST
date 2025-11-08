package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.CardCreationRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardDto> createCard(@RequestBody CardCreationRequest request,
                                              @RequestParam UUID userId) {
        return ResponseEntity.ok(cardService.createCard(userId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCard(@RequestParam UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/activate/{cardId}")
    public ResponseEntity<CardDto> activateCard(@PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.activateCard(cardId));
    }

    @PutMapping("/block/{cardId}")
    public ResponseEntity<CardDto> blockCard(@PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserCards(@PathVariable UUID userId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) CardStatus status) {
        Page<CardDto> dtoPage = cardService.getUserCards(userId, status, page, size);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CardDto>> getAllCards() {
        List<CardDto> listDto = cardService.getAllCards();
        return ResponseEntity.ok(listDto);
    }

}
