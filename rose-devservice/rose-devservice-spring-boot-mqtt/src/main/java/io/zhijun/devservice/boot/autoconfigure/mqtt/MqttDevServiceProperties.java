package io.zhijun.devservice.boot.autoconfigure.mqtt;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * MQTT dev service properties.
 */
@ConfigurationProperties(prefix = MqttDevServiceProperties.CONFIG_PREFIX)
public class MqttDevServiceProperties extends BaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.mqtt";

    public MqttDevServiceProperties() {
        setImageName("hivemq/hivemq-ce:2024.1");
        setStartupTimeout(Duration.ofSeconds(60));
    }
}
