package io.zhijun.devservice.boot.autoconfigure.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * MQTT dev service properties.
 */
@ConfigurationProperties(prefix = MqttDevServiceProperties.CONFIG_PREFIX)
public class MqttDevServiceProperties extends BaseDevServiceProperties {

    public static final String SERVICE_NAME = "mqtt";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "hivemq/hivemq-ce:2024.1";

    public MqttDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setStartupTimeout(SLOW_STARTUP_TIMEOUT);
    }
}
