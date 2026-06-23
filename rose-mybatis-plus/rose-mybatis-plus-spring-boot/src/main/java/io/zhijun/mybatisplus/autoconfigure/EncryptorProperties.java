package io.zhijun.mybatisplus.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Field encryption properties.
 */
@ConfigurationProperties(prefix = "rose.mybatis-plus.encryptor")
public class EncryptorProperties {

    /**
     * Default secret for {@code secretRef = default}.
     */
    private String password = "";

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
