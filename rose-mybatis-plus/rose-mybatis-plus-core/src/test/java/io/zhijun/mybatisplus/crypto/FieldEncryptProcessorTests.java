package io.zhijun.mybatisplus.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FieldEncryptProcessorTests {

    private final DefaultFieldEncryptor encryptor = new DefaultFieldEncryptor();

  private final EncryptionKeyResolver keyResolver = secretRef -> null;

    @Test
    void shouldEncryptAnnotatedFieldBeforeWrite() {
        SampleAccount account = new SampleAccount();
        account.setPhone("13800138000");

        FieldEncryptProcessor.processWrite(account, encryptor, keyResolver);

        assertThat(account.getPhone()).isNotEqualTo("13800138000");
    }

    @Test
    void shouldDecryptAnnotatedFieldAfterRead() {
        SampleAccount account = new SampleAccount();
        account.setPhone(encryptor.encrypt(EncryptAlgorithm.BASE64, null, "13800138000"));

        FieldEncryptProcessor.processRead(account, encryptor, keyResolver);

        assertThat(account.getPhone()).isEqualTo("13800138000");
    }

    static class SampleAccount {
        @FieldEncrypt
        private String phone;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
