package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardNumberMaskerTest {

    private CardNumberMasker masker;

    @BeforeEach
    void setUp() {
        masker = new CardNumberMasker();
    }

    @Test
    @DisplayName("Should mask correctly when valid last 4 digits are provided")
    void maskCardNumber_validLast4() {
        String result = masker.maskCardNumber("1234");
        assertEquals("**** **** **** 1234", result);
    }

    @Test
    @DisplayName("Should extract last 4 digits when more digits are passed")
    void maskCardNumber_moreThan4Digits() {
        String result = masker.maskCardNumber("56789012");
        assertEquals("**** **** **** 9012", result);
    }

    @Test
    @DisplayName("Should remove non-digit characters and still show last 4")
    void maskCardNumber_withNonDigits() {
        String result = masker.maskCardNumber("AB12-34-56CD");
        assertEquals("**** **** **** 3456", result);
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    void maskCardNumber_whenNull() {
        assertEquals("**** **** **** ****", masker.maskCardNumber(null));
    }

    @Test
    @DisplayName("Should handle empty string gracefully")
    void maskCardNumber_whenEmpty() {
        assertEquals("**** **** **** ****", masker.maskCardNumber(""));
    }

    @Test
    @DisplayName("Should mask correctly when only partial digits provided (<4)")
    void maskCardNumber_whenTooShort() {
        String result = masker.maskCardNumber("89");
        assertEquals("**** **** **** 89", result);
    }

    @Test
    @DisplayName("Should handle input containing spaces")
    void maskCardNumber_withSpaces() {
        String result = masker.maskCardNumber("  3456 ");
        assertEquals("**** **** **** 3456", result);
    }
}
