package io.zhijun.mybatisplus.core.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DefaultFieldEncryptorTests {

    private final DefaultFieldEncryptor encryptor = new DefaultFieldEncryptor();

    private static final String SECRET = "test-secret-key";

    @Test
    void base64RoundTrip() {
        String plaintext = "sensitive-value";
        String encrypted = encryptor.encrypt(EncryptAlgorithm.BASE64, SECRET, plaintext);
        assertThat(encrypted).isNotEqualTo(plaintext);
        assertThat(encryptor.decrypt(EncryptAlgorithm.BASE64, SECRET, encrypted)).isEqualTo(plaintext);
    }

    @Test
    void aesRoundTrip() {
        String plaintext = "sensitive-value";
        String encrypted = encryptor.encrypt(EncryptAlgorithm.AES, SECRET, plaintext);
        assertThat(encrypted).isNotEqualTo(plaintext);
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, SECRET, encrypted)).isEqualTo(plaintext);
    }

    @Test
    void aesCiphertextDiffersForSamePlaintext() {
        String plaintext = "sensitive-value";
        String first = encryptor.encrypt(EncryptAlgorithm.AES, SECRET, plaintext);
        String second = encryptor.encrypt(EncryptAlgorithm.AES, SECRET, plaintext);
        assertThat(first).isNotEqualTo(second);
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, SECRET, first)).isEqualTo(plaintext);
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, SECRET, second)).isEqualTo(plaintext);
    }

    @Test
    void aesRoundTripHandlesUnicode() {
        String plaintext = "敏感数据-émojis-🔐";
        String encrypted = encryptor.encrypt(EncryptAlgorithm.AES, SECRET, plaintext);
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, SECRET, encrypted)).isEqualTo(plaintext);
    }

    @Test
    void encryptNullReturnsNull() {
        assertThat(encryptor.encrypt(EncryptAlgorithm.AES, SECRET, null)).isNull();
        assertThat(encryptor.encrypt(EncryptAlgorithm.BASE64, SECRET, null)).isNull();
    }

    @Test
    void encryptBlankReturnsBlank() {
        assertThat(encryptor.encrypt(EncryptAlgorithm.AES, SECRET, "")).isEmpty();
        assertThat(encryptor.encrypt(EncryptAlgorithm.AES, SECRET, "   ")).isEqualTo("   ");
    }

    @Test
    void decryptNullReturnsNull() {
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, SECRET, null)).isNull();
        assertThat(encryptor.decrypt(EncryptAlgorithm.BASE64, SECRET, null)).isNull();
    }

    @Test
    void decryptBlankReturnsBlank() {
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, SECRET, "")).isEmpty();
        assertThat(encryptor.decrypt(EncryptAlgorithm.AES, SECRET, "   ")).isEqualTo("   ");
    }

    @Test
    void aesEncryptRejectsBlankSecret() {
        assertThatThrownBy(() -> encryptor.encrypt(EncryptAlgorithm.AES, "", "value"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("secret");
        assertThatThrownBy(() -> encryptor.encrypt(EncryptAlgorithm.AES, "   ", "value"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aesDecryptRejectsBlankSecret() {
        String encrypted = encryptor.encrypt(EncryptAlgorithm.AES, SECRET, "value");
        assertThatThrownBy(() -> encryptor.decrypt(EncryptAlgorithm.AES, "", encrypted))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aesDecryptWithWrongSecretThrows() {
        String encrypted = encryptor.encrypt(EncryptAlgorithm.AES, SECRET, "value");
        assertThatThrownBy(() -> encryptor.decrypt(EncryptAlgorithm.AES, "wrong-secret", encrypted))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AES decryption failed");
    }

    @Test
    void encryptInvokesToStringOnObject() {
        Object value = 12345;
        String encrypted = encryptor.encrypt(EncryptAlgorithm.BASE64, SECRET, value);
        assertThat(encryptor.decrypt(EncryptAlgorithm.BASE64, SECRET, encrypted)).isEqualTo("12345");
    }
}
