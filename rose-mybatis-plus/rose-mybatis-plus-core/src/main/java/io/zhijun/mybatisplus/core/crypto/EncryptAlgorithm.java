package io.zhijun.mybatisplus.core.crypto;

/**
 * Supported field encryption algorithms.
 */
public enum EncryptAlgorithm {

    /**
     * BASE64 encoding — not encryption. Suitable for obfuscation in non-sensitive contexts.
     */
    BASE64,

    /**
     * AES-256 in GCM mode (authenticated encryption with associated data).
     * The secret is derived via SHA-256. Each encryption uses a random 12-byte IV.
     * Ciphertext format: Base64(iv ‖ ciphertext ‖ tag).
     */
    AES
}
