package io.zhijun.devservice.boot.autoconfigure.rabbitmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * RabbitMQ dev service properties.
 */
@ConfigurationProperties(prefix = RabbitMqDevServiceProperties.CONFIG_PREFIX)
public class RabbitMqDevServiceProperties extends BaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.rabbitmq";

    /** Fixed host port for the RabbitMQ management console; 0 selects a random port. */
    private int managementConsolePort = 0;

    public RabbitMqDevServiceProperties() {
        setImageName("rabbitmq:3.12-management-alpine");
        setShared(true);
    }

    public int getManagementConsolePort() {
        return managementConsolePort;
    }

    public void setManagementConsolePort(int managementConsolePort) {
        this.managementConsolePort = managementConsolePort;
    }
}
