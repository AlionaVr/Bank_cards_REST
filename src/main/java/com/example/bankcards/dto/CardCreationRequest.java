package com.example.bankcards.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardCreationRequest {
    private String cardHolderName;
    private BigDecimal initialBalance;
}
