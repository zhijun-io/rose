package io.zhijun.mybatisplus.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Field encryption properties.
 */
@ConfigurationProperties(prefix = EncryptorProperties.CONFIG_PREFIX)
public class EncryptorProperties {

    public static final String CONFIG_PREFIX = "rose.mybatis-plus.encryptor";

    /**
     * Default secret for {@code secretRef = default}.
     * <p>
     * Must be configured when using encryption algorithms that require a key.
     * When left as {@code null} (the default), only keyless algorithms such as
     * {@link io.zhijun.mybatisplus.core.crypto.EncryptAlgorithm#BASE64} will function.
     */
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
