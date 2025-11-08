package com.example.bankcards.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardCreationRequest {

    @NotBlank(message = "Card holder name is required")
    @Size(min = 16, max = 50, message = "Card holder name must be between 16 and 50 characters")
    private String cardHolderName;

    @DecimalMin(value = "0.0", message = "Initial balance cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid balance format")
    private BigDecimal initialBalance;
}
