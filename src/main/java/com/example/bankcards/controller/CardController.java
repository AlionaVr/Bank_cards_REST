package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreationRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping
    public ResponseEntity<Void> createCard(@RequestBody CardCreationRequest request,
                                           @RequestParam Long userId) {
        cardService.createCard(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCard(@RequestParam Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/activate/{cardId}")
    public ResponseEntity<Void> activateCard(@PathVariable Long cardId) {
        cardService.activateCard(cardId);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/block/{cardId}")
    public ResponseEntity<Void> blockCard(@PathVariable Long cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserCards(@PathVariable Long userId) {
        List<Card> cards = cardService.getUserCards(userId);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCards() {
        List<Card> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }

}
