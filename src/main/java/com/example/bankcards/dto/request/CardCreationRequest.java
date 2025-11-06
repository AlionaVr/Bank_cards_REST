package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardCreationRequest {

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    private BigDecimal initialBalance;
}
