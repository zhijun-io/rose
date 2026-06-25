package io.zhijun.devservice.boot.autoconfigure.postgresql;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.JdbcDevServiceProperties;

/**
 * PostgreSQL dev service properties.
 */
@ConfigurationProperties(prefix = PostgresqlDevServiceProperties.CONFIG_PREFIX)
public class PostgresqlDevServiceProperties extends JdbcDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.postgresql";

    public PostgresqlDevServiceProperties() {
        setImageName("postgres:18-alpine");
    }
}
