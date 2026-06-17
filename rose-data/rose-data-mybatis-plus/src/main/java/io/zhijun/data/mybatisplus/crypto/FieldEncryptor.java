package io.zhijun.data.mybatisplus.crypto;

import org.springframework.lang.Nullable;

/**
 * Encrypts and decrypts annotated field values.
 */
public interface FieldEncryptor {

    @Nullable
    String encrypt(EncryptAlgorithm algorithm, @Nullable String secret, @Nullable Object rawValue);

    @Nullable
    String decrypt(EncryptAlgorithm algorithm, @Nullable String secret, @Nullable Object storedValue);
}
