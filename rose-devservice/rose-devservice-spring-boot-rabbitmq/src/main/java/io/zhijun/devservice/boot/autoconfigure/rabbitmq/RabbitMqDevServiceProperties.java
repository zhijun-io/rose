package io.zhijun.devservice.boot.autoconfigure.rabbitmq;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RabbitMQ dev service properties.
 */
@ConfigurationProperties(prefix = RabbitMqDevServiceProperties.CONFIG_PREFIX)
public class RabbitMqDevServiceProperties extends BaseDevServiceProperties {

    public static final String SERVICE_NAME = "rabbitmq";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "rabbitmq:3.12-management-alpine";

    /** Fixed host port for the RabbitMQ management console; 0 selects a random port. */
    private int managementConsolePort = RANDOM_PORT;

    public RabbitMqDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setShared(true);
    }

    public int getManagementConsolePort() {
        return managementConsolePort;
    }

    public void setManagementConsolePort(int managementConsolePort) {
        this.managementConsolePort = managementConsolePort;
    }
}
