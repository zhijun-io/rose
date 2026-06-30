package io.zhijun.devservice.boot.autoconfigure.artemis;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.MessagingDevServiceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ActiveMQ Artemis dev service properties.
 */
@ConfigurationProperties(prefix = ArtemisDevServiceProperties.CONFIG_PREFIX)
public class ArtemisDevServiceProperties extends MessagingDevServiceProperties {

    public static final String SERVICE_NAME = "artemis";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "apache/activemq-artemis:2.31.2-alpine";

    public ArtemisDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setShared(true);
        setStartupTimeout(SLOW_STARTUP_TIMEOUT);
    }
}
