package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", unique = true, nullable = false, length = 16)
    private String cardNumber;

    @Column(nullable = false)
    private String cardHolderName;

    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    public boolean isActive() {
        return (status == CardStatus.ACTIVE);
    }
    public boolean isBlocked() {
        return (status == CardStatus.BLOCKED);
    }
    public boolean isNotExpired() {
        return (status != CardStatus.EXPIRED);
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
        expiryDate = createdDate.plusYears(4);
        status = CardStatus.ACTIVE;
    }
}
