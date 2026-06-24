package io.zhijun.devservice.boot.autoconfigure.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.AbstractBaseDevServiceProperties;

/**
 * MQTT dev service properties.
 */
@ConfigurationProperties(prefix = MqttDevServiceProperties.CONFIG_PREFIX)
public class MqttDevServiceProperties extends AbstractBaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.mqtt";

    public MqttDevServiceProperties() {
        setImageName("hivemq/hivemq-ce:2024.1");
        setStartupTimeout(java.time.Duration.ofSeconds(60));
    }
}
