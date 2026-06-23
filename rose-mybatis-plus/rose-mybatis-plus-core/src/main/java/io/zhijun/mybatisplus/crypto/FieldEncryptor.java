package io.zhijun.mybatisplus.crypto;

/**
 * Encrypts and decrypts annotated field values.
 */
public interface FieldEncryptor {

    String encrypt(EncryptAlgorithm algorithm, String secret, Object rawValue);

    String decrypt(EncryptAlgorithm algorithm, String secret, Object storedValue);
}
