package io.zhijun.devservice.boot.autoconfigure.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * Redis dev service properties.
 */
@ConfigurationProperties(prefix = RedisDevServiceProperties.CONFIG_PREFIX)
public class RedisDevServiceProperties extends BaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.redis";

    public RedisDevServiceProperties() {
        setImageName("redis:7-alpine");
    }
}
