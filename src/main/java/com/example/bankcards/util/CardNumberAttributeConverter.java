package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
@Slf4j
public class CardNumberAttributeConverter implements AttributeConverter<String, String> {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 16;
    private static final int NONCE_LENGTH = 12;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${PAN_ENCRYPTION_KEY_BASE64}")
    private String keyB64;

    public CardNumberAttributeConverter() {

        String keyB64 = System.getenv("PAN_ENCRYPTION_KEY_BASE64");
        if (keyB64 == null || keyB64.isBlank()) {
            log.error("PAN encryption key not found in env PAN_ENCRYPTION_KEY_BASE64");
            throw new IllegalStateException("PAN encryption key not configured");
        }
        byte[] keyBytes = Base64.getDecoder().decode(keyB64);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            log.error("Invalid PAN encryption key length: {}", keyBytes.length);
            throw new IllegalStateException("Invalid PAN encryption key length");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            // nonce (IV)
            byte[] nonce = new byte[NONCE_LENGTH];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(attribute.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            byte[] combined = new byte[nonce.length + ciphertext.length];
            System.arraycopy(nonce, 0, combined, 0, nonce.length);
            System.arraycopy(ciphertext, 0, combined, nonce.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Error encrypting PAN", e);
            throw new RuntimeException("Error encrypting PAN", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);
            if (combined.length < NONCE_LENGTH + GCM_TAG_LENGTH) {
                throw new IllegalArgumentException("Invalid ciphertext");
            }
            byte[] nonce = new byte[NONCE_LENGTH];
            System.arraycopy(combined, 0, nonce, 0, NONCE_LENGTH);
            byte[] ciphertext = new byte[combined.length - NONCE_LENGTH];
            System.arraycopy(combined, NONCE_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plain = cipher.doFinal(ciphertext);
            return new String(plain, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting PAN", e);
            throw new RuntimeException("Error decrypting PAN", e);
        }
    }
}
