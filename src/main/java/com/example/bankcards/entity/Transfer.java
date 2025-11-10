package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfers", indexes = {
        @Index(name = "idx_transfer_from_card", columnList = "from_card_id"),
        @Index(name = "idx_transfer_to_card", columnList = "to_card_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @PrePersist
    protected void onCreate() {
        transferDate = LocalDateTime.now();
    }
}
