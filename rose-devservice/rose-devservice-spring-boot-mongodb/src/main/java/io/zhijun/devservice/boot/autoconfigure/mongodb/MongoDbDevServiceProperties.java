package io.zhijun.devservice.boot.autoconfigure.mongodb;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * MongoDB dev service properties.
 */
@ConfigurationProperties(prefix = MongoDbDevServiceProperties.CONFIG_PREFIX)
public class MongoDbDevServiceProperties extends BaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.mongodb";

    public MongoDbDevServiceProperties() {
        setImageName("mongo:6.0");
    }
}
