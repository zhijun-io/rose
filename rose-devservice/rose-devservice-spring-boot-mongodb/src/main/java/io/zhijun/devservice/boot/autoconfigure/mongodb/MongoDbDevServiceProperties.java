package io.zhijun.devservice.boot.autoconfigure.mongodb;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MongoDB dev service properties.
 */
@ConfigurationProperties(prefix = MongoDbDevServiceProperties.CONFIG_PREFIX)
public class MongoDbDevServiceProperties extends BaseDevServiceProperties {

    public static final String SERVICE_NAME = "mongodb";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "mongo:6.0";

    public MongoDbDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setShared(true);
    }
}
