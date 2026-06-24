package io.zhijun.mybatisplus.core.crypto;

import static org.assertj.core.api.Assertions.assertThat;

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
}
