package com.example.bankcards.service;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransferDto transferBetweenCards(TransferRequest request) {
        User currentUser = getCurrentUser();
        Card fromCard = findCardById(request.getFromCardId());
        Card toCard = findCardById(request.getToCardId());

        validateCardsForTransfer(request, fromCard, toCard, currentUser);

        fromCard.subtractBalance(request.getAmount());
        toCard.addBalance(request.getAmount());

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transfer transfer = Transfer.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        transferRepository.save(transfer);
        return new TransferDto(transfer);
    }

    @Transactional(readOnly = true)
    public Page<TransferDto> getTransferHistory(int page, int size, UUID cardId) {
        User currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        Pageable pageable = PageRequest.of(page, size, Sort.by("transferDate").descending());
        Page<Transfer> transfers;

        if (cardId != null) {
            Card card = findCardById(cardId);

            if (!isAdmin && !card.getOwner().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You can only view transfers for your own cards");
            }

            transfers = transferRepository.findByCardId(cardId, pageable);
        } else {
            if (isAdmin) {
                transfers = transferRepository.findAll(pageable);
            } else {
                UUID userId = currentUser.getId();
                transfers = transferRepository.findByUserId(userId, pageable);
            }
        }
        return transfers.map(TransferDto::new);
    }

    private Card findCardById(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    private void validateCardsForTransfer(TransferRequest request, Card fromCard, Card toCard, User currentUser) {
        if (fromCard.getId().equals(toCard.getId())) {
            throw new CardOperationException("Cannot transfer to the same card");
        }

        if (!fromCard.getOwner().getId().equals(currentUser.getId()) ||
                !toCard.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only transfer between your own cards");
        }

        if (!fromCard.isActive() || !toCard.isActive()) {
            throw new CardOperationException(String.format("Card is not active. Status fromCard: %s. Status toCard: %s", fromCard.getStatus(), toCard));
        }
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new CardOperationException("Insufficient funds on source card");
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + login));
    }
}