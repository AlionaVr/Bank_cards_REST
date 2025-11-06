package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreationRequest;
import com.example.bankcards.entity.Card;
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
    public ResponseEntity<Void> createCard(@RequestBody CardCreationRequest request,
                                           @RequestParam UUID userId) {
        cardService.createCard(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCard(@RequestParam UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/activate/{cardId}")
    public ResponseEntity<Void> activateCard(@PathVariable UUID cardId) {
        cardService.activateCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/block/{cardId}")
    public ResponseEntity<Void> blockCard(@PathVariable UUID cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserCards(@PathVariable UUID userId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) CardStatus status) {
        Page<Card> cardsPage = cardService.getUserCards(userId, status, page, size);
        return ResponseEntity.ok(cardsPage);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCards() {
        List<Card> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }

}
