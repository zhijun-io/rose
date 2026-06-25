package io.zhijun.devservice.boot.autoconfigure.mysql;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.JdbcDevServiceProperties;

/**
 * MySQL dev service properties.
 */
@ConfigurationProperties(prefix = MySqlDevServiceProperties.CONFIG_PREFIX)
public class MySqlDevServiceProperties extends JdbcDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.mysql";

    public MySqlDevServiceProperties() {
        setImageName("mysql:8.0");
    }
}
