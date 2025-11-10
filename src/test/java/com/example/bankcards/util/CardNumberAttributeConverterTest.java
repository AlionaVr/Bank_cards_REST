package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardNumberAttributeConverterTest {

    private CardNumberAttributeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CardNumberAttributeConverter();
    }

    @Test
    @DisplayName("Should encrypt correctly when valid card number is provided")
    void convertToDatabaseColumn_encryptsValue() {
        String plain = "1234567890123456";
        String encrypted = converter.convertToDatabaseColumn(plain);

        assertNotNull(encrypted);
        assertNotEquals(plain, encrypted);
    }

    @Test
    @DisplayName("Should decrypt correctly when valid encrypted value is provided")
    void convertToEntityAttribute_decryptsValue() {
        String plain = "1234567890123456";
        String encrypted = converter.convertToDatabaseColumn(plain);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertEquals(plain, decrypted);
    }

    @Test
    @DisplayName("Should return null when null is provided")
    void convertToDatabaseColumn_null_returnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    @DisplayName("Should return null when null is provided")
    void convertToEntityAttribute_null_returnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }
}
