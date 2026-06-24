package io.zhijun.mybatisplus.core.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

/**
 * Default field encryptor supporting BASE64 encoding and AES-256/GCM encryption.
 * <p>
 * BASE64 is encoding, not encryption — use it only for obfuscation.
 * <p>
 * AES uses GCM mode with a 12-byte random IV per encryption. The secret is
 * derived through SHA-256 to produce a 256-bit key. Ciphertext is stored as
 * Base64(iv ‖ ciphertext ‖ tag).
 */
public class DefaultFieldEncryptor implements FieldEncryptor {

    private static final int AES_KEY_SIZE_BITS = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String encrypt(EncryptAlgorithm algorithm, String secret, Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        String text = rawValue.toString();
        if (StringUtils.isBlank(text)) {
            return text;
        }
        if (algorithm == EncryptAlgorithm.BASE64) {
            return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        }
        if (algorithm == EncryptAlgorithm.AES) {
            return encryptAesGcm(requireSecret(algorithm, secret), text);
        }
        throw new IllegalArgumentException("Unsupported encrypt algorithm: " + algorithm);
    }

    @Override
    public String decrypt(EncryptAlgorithm algorithm, String secret, Object storedValue) {
        if (storedValue == null) {
            return null;
        }
        String text = storedValue.toString();
        if (StringUtils.isBlank(text)) {
            return text;
        }
        if (algorithm == EncryptAlgorithm.BASE64) {
            return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
        }
        if (algorithm == EncryptAlgorithm.AES) {
            return decryptAesGcm(requireSecret(algorithm, secret), text);
        }
        throw new IllegalArgumentException("Unsupported encrypt algorithm: " + algorithm);
    }

    private static String encryptAesGcm(String secret, String plaintext) {
        try {
            byte[] keyBytes = deriveKey(secret);
            byte[] iv = new byte[GCM_IV_LENGTH];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(keyBytes, "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception ex) {
            throw new IllegalStateException("AES encryption failed", ex);
        }
    }

    private static String decryptAesGcm(String secret, String encoded) {
        try {
            byte[] combined = Base64.getDecoder().decode(encoded);
            byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

            byte[] keyBytes = deriveKey(secret);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(keyBytes, "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("AES decryption failed", ex);
        }
    }

    private static byte[] deriveKey(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(hash, AES_KEY_SIZE_BITS / 8);
        } catch (Exception ex) {
            throw new IllegalStateException("Key derivation failed", ex);
        }
    }

    private static String requireSecret(EncryptAlgorithm algorithm, String secret) {
        if (StringUtils.isBlank(secret)) {
            throw new IllegalArgumentException(
                    "A non-empty secret is required for " + algorithm + " encryption");
        }
        return secret;
    }
}
