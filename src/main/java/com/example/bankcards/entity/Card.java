package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cards", indexes = {
        @Index(name = "idx_card_owner", columnList = "user_id"),
        @Index(name = "idx_card_status", columnList = "status")})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "card_number", unique = true, nullable = false, length = 16)
    private String cardNumber;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CardStatus status = CardStatus.ACTIVE;

    public boolean isActive() {
        return (status == CardStatus.ACTIVE);
    }

    public boolean isBlocked() {
        return (status == CardStatus.BLOCKED);
    }

    public boolean isExpired() {
        return (status == CardStatus.EXPIRED);
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
        expiryDate = createdDate.plusYears(4);
        status = CardStatus.ACTIVE;
    }

    @PostLoad
    protected void checkExpiration() {
        if (isExpired() && status != CardStatus.EXPIRED) {
            status = CardStatus.EXPIRED;
        }
    }
}
