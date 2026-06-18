package io.zhijun.mybatisplus.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * Default field encryptor. BASE64 is encoding, not encryption.
 */
public class DefaultFieldEncryptor implements FieldEncryptor {

    @Override
    public @Nullable String encrypt(EncryptAlgorithm algorithm, @Nullable String secret, @Nullable Object rawValue) {
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
        throw new IllegalArgumentException("Unsupported encrypt algorithm: " + algorithm);
    }

    @Override
    public @Nullable String decrypt(EncryptAlgorithm algorithm, @Nullable String secret, @Nullable Object storedValue) {
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
        throw new IllegalArgumentException("Unsupported encrypt algorithm: " + algorithm);
    }
}
