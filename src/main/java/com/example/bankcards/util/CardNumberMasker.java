package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardNumberMasker {
    private static final int CARD_NUMBER_LENGTH = 16;
    private static final int QUANTITY_VISIBLE_DIGITS = 4;
    private static final char MASK_CHAR = '*';

    public String maskCardNumber(String last4) {
        if (last4 == null || last4.isEmpty()) {
            return "**** **** **** ****";
        }
        String visible = last4.replaceAll("\\D", "");
        int maskedLength = CARD_NUMBER_LENGTH - QUANTITY_VISIBLE_DIGITS;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maskedLength; i++) {
            sb.append(MASK_CHAR);
        }
        sb.append(visible.substring(QUANTITY_VISIBLE_DIGITS));

        return sb.toString().replaceAll("(.{4})", "$1 ").trim();
    }
}
