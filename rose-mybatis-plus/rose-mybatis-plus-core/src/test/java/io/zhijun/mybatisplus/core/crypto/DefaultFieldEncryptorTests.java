package io.zhijun.mybatisplus.core.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
