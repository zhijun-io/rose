package io.zhijun.mybatisplus.crypto;

/**
 * Resolves secret material for field encryption.
 */
@FunctionalInterface
public interface EncryptionKeyResolver {

    String resolve(String secretRef);
}
