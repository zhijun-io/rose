package io.zhijun.mybatisplus.crypto;

import org.springframework.lang.Nullable;

/**
 * Resolves secret material for field encryption.
 */
@FunctionalInterface
public interface EncryptionKeyResolver {

    @Nullable
    String resolve(String secretRef);
}
