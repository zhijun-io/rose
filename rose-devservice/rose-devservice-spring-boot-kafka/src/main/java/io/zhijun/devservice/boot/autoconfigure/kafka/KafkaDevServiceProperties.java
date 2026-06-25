package io.zhijun.devservice.boot.autoconfigure.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * Kafka dev service properties.
 */
@ConfigurationProperties(prefix = KafkaDevServiceProperties.CONFIG_PREFIX)
public class KafkaDevServiceProperties extends BaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.kafka";

    public KafkaDevServiceProperties() {
        setImageName("confluentinc/cp-kafka:7.4.0");
        setShared(true);
    }
}
