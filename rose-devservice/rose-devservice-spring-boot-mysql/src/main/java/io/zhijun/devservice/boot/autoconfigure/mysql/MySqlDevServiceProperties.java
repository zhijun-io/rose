package io.zhijun.devservice.boot.autoconfigure.mysql;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.JdbcDevServiceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MySQL dev service properties.
 */
@ConfigurationProperties(prefix = MySqlDevServiceProperties.CONFIG_PREFIX)
public class MySqlDevServiceProperties extends JdbcDevServiceProperties {

    public static final String SERVICE_NAME = "mysql";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "mysql:8.0";

    public MySqlDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setShared(true);
    }
}
