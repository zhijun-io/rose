package io.zhijun.devservice.boot.autoconfigure.rabbitmq;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.test.BaseDevServicePropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link RabbitMqDevServiceProperties}.
 */
class RabbitMqDevServicePropertiesTests extends BaseDevServicePropertiesTests<RabbitMqDevServiceProperties> {

    @Override
    protected RabbitMqDevServiceProperties createProperties() {
        return new RabbitMqDevServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RabbitMqDevServiceProperties.DEFAULT_IMAGE_NAME)
                .shared(true)
                .build();
    }

    @Test
    void shouldCreateInstanceWithServiceSpecificDefaultValues() {
        RabbitMqDevServiceProperties properties = createProperties();
        assertThat(properties.getManagementConsolePort()).isEqualTo(0);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        RabbitMqDevServiceProperties properties = createProperties();
        properties.setManagementConsolePort(RabbitMqContainer.HTTP_PORT);
        assertThat(properties.getManagementConsolePort()).isEqualTo(RabbitMqContainer.HTTP_PORT);
    }

}
