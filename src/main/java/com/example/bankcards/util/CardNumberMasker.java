package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardNumberMasker {

    public String maskCardNumber(String last4) {
        if (last4 == null || last4.isEmpty()) {
            return "**** **** **** ****";
        }
        String visible = last4.replaceAll("\\D", "");
        if (visible.length() > 4) {
            visible = visible.substring(visible.length() - 4);
        }

        return "**** **** **** " + visible;
    }
}
