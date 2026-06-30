package io.zhijun.devservice.boot.autoconfigure.kafka;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kafka dev service properties.
 */
@ConfigurationProperties(prefix = KafkaDevServiceProperties.CONFIG_PREFIX)
public class KafkaDevServiceProperties extends BaseDevServiceProperties {

    public static final String SERVICE_NAME = "kafka";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "confluentinc/cp-kafka:7.4.0";

    public KafkaDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setShared(true);
    }
}
