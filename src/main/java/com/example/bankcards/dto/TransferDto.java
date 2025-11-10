package com.example.bankcards.dto;

import com.example.bankcards.entity.Transfer;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Schema(description = "Transfer information")
public class TransferDto {
    private UUID id;
    private UUID fromCardId;
    private UUID toCardId;
    private BigDecimal amount;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transferDate;

    public TransferDto(Transfer transfer) {
        this.id = transfer.getId();
        this.fromCardId = transfer.getFromCard().getId();
        this.toCardId = transfer.getToCard().getId();
        this.amount = transfer.getAmount();
        this.description = transfer.getDescription();
        this.transferDate = transfer.getTransferDate();
    }
}
