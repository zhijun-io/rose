package io.zhijun.devservice.boot.autoconfigure.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * Redis dev service properties.
 */
@ConfigurationProperties(prefix = RedisDevServiceProperties.CONFIG_PREFIX)
public class RedisDevServiceProperties extends BaseDevServiceProperties {

    public static final String SERVICE_NAME = "redis";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "redis:7-alpine";

    public RedisDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setShared(true);
    }
}
