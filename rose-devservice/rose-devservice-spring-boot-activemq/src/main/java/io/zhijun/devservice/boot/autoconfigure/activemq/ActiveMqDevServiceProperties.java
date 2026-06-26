package io.zhijun.devservice.boot.autoconfigure.activemq;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.MessagingDevServiceProperties;

/**
 * ActiveMQ Classic dev service properties.
 */
@ConfigurationProperties(prefix = ActiveMqDevServiceProperties.CONFIG_PREFIX)
public class ActiveMqDevServiceProperties extends MessagingDevServiceProperties {

    public static final String SERVICE_NAME = "activemq";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "apache/activemq-classic:5.18.3";

    public ActiveMqDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setShared(true);
        setStartupTimeout(SLOW_STARTUP_TIMEOUT);
    }
}
