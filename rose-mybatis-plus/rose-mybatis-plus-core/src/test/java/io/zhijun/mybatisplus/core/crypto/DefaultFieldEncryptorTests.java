package io.zhijun.mybatisplus.core.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.Test;

class DefaultFieldEncryptorTests {

    private final DefaultFieldEncryptor encryptor = new DefaultFieldEncryptor();

    @Test
    void shouldRoundTripBase64Value() {
        String encrypted = encryptor.encrypt(EncryptAlgorithm.BASE64, null, "13800138000");
        assertThat(encrypted).isNotEqualTo("13800138000");

        String decrypted = encryptor.decrypt(EncryptAlgorithm.BASE64, null, encrypted);
        assertThat(decrypted).isEqualTo("13800138000");
    }

    @Test
    void shouldRoundTripAesValue() {
        String secret = "my-secret-key";
        String plaintext = "sensitive-data-123";

        String encrypted = encryptor.encrypt(EncryptAlgorithm.AES, secret, plaintext);
        assertThat(encrypted).isNotEqualTo(plaintext);

        String decrypted = encryptor.decrypt(EncryptAlgorithm.AES, secret, encrypted);
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void shouldProduceDifferentCiphertextsForSamePlaintext() {
        String secret = "my-secret-key";
        String plaintext = "same-value";

        String encrypted1 = encryptor.encrypt(EncryptAlgorithm.AES, secret, plaintext);
        String encrypted2 = encryptor.encrypt(EncryptAlgorithm.AES, secret, plaintext);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, secret, encrypted1)).isEqualTo(plaintext);
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, secret, encrypted2)).isEqualTo(plaintext);
    }

    @Test
    void shouldFailAesWithBlankSecret() {
        assertThatThrownBy(() -> encryptor.encrypt(EncryptAlgorithm.AES, "", "data"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-empty secret");
    }

    @Test
    void shouldFailAesWithNullSecret() {
        assertThatThrownBy(() -> encryptor.encrypt(EncryptAlgorithm.AES, null, "data"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-empty secret");
    }

    @Test
    void shouldFailAesDecryptionWithWrongSecret() {
        String encrypted = encryptor.encrypt(EncryptAlgorithm.AES, "correct-secret", "data");
        assertThatThrownBy(() -> encryptor.decrypt(EncryptAlgorithm.AES, "wrong-secret", encrypted))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AES decryption failed");
    }

    @Test
    void shouldHandleNullAndBlankValues() {
        assertThat(encryptor.encrypt(EncryptAlgorithm.AES, "secret", null)).isNull();
        assertThat(encryptor.encrypt(EncryptAlgorithm.AES, "secret", "")).isEmpty();
        assertThat(encryptor.encrypt(EncryptAlgorithm.AES, "secret", "  ")).isEqualTo("  ");
    }

    @Test
    void shouldEncodeKnownBase64Vector() {
        // "hello" UTF-8 → Base64
        assertThat(encryptor.encrypt(EncryptAlgorithm.BASE64, null, "hello")).isEqualTo("aGVsbG8=");
    }

    @Test
    void shouldDecodeKnownBase64Vector() {
        assertThat(encryptor.decrypt(EncryptAlgorithm.BASE64, null, "aGVsbG8=")).isEqualTo("hello");
    }

    @Test
    void shouldFailDecodingInvalidBase64() {
        assertThatThrownBy(() -> encryptor.decrypt(EncryptAlgorithm.BASE64, null, "@@@not-base64@@@"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUseRandomIvAcrossAesEncryptions() {
        String secret = "my-secret-key";
        byte[] iv1 = extractIv(encryptor.encrypt(EncryptAlgorithm.AES, secret, "same-value"));
        byte[] iv2 = extractIv(encryptor.encrypt(EncryptAlgorithm.AES, secret, "same-value"));
        assertThat(iv1).isNotEqualTo(iv2);
    }

    @Test
    void shouldFailWhenAesCiphertextIsTampered() {
        String encrypted = encryptor.encrypt(EncryptAlgorithm.AES, "secret", "data");
        byte[] bytes = Base64.getDecoder().decode(encrypted);
        // flip a byte in the ciphertext/tag region (after the 12-byte IV)
        bytes[bytes.length - 1] ^= 0x01;
        String tampered = Base64.getEncoder().encodeToString(bytes);
        assertThatThrownBy(() -> encryptor.decrypt(EncryptAlgorithm.AES, "secret", tampered))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AES decryption failed");
    }

    @Test
    void shouldFailWhenIvIsTampered() {
        String encrypted = encryptor.encrypt(EncryptAlgorithm.AES, "secret", "data");
        byte[] bytes = Base64.getDecoder().decode(encrypted);
        bytes[0] ^= 0x01; // corrupt IV
        String tampered = Base64.getEncoder().encodeToString(bytes);
        assertThatThrownBy(() -> encryptor.decrypt(EncryptAlgorithm.AES, "secret", tampered))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AES decryption failed");
    }

    @Test
    void shouldDecryptNullAndBlankValues() {
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, "secret", null)).isNull();
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, "secret", "")).isEmpty();
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, "secret", "  ")).isEqualTo("  ");
    }

    @Test
    void shouldRoundTripNonStringObject() {
        String encrypted = encryptor.encrypt(EncryptAlgorithm.AES, "secret", 123L);
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, "secret", encrypted)).isEqualTo("123");
    }

    @Test
    void shouldFailWithUnsupportedAlgorithm() {
        assertThatThrownBy(() -> encryptor.encrypt(null, "secret", "data"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported encrypt algorithm");
    }

    private static byte[] extractIv(String encrypted) {
        byte[] combined = Base64.getDecoder().decode(encrypted);
        return Arrays.copyOfRange(combined, 0, 12);
    }
}
