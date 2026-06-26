package io.zhijun.devservice.boot.autoconfigure.postgresql;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.JdbcDevServiceProperties;

/**
 * PostgreSQL dev service properties.
 */
@ConfigurationProperties(prefix = PostgresqlDevServiceProperties.CONFIG_PREFIX)
public class PostgresqlDevServiceProperties extends JdbcDevServiceProperties {

    public static final String SERVICE_NAME = "postgresql";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "postgres:18-alpine";

    public PostgresqlDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setShared(true);
    }
}
