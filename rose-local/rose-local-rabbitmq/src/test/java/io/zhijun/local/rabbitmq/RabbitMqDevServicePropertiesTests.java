package io.zhijun.local.rabbitmq;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.BaseLocalServicePropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RabbitMqLocalServiceProperties}.
 */
class RabbitMqDevServicePropertiesTests extends BaseLocalServicePropertiesTests<RabbitMqLocalServiceProperties> {

    @Override
    protected RabbitMqLocalServiceProperties createProperties() {
        return new RabbitMqLocalServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseRabbitMqContainer.COMPATIBLE_IMAGE_NAME)
                .shared(true)
                .build();
    }

    @Test
    void shouldCreateInstanceWithServiceSpecificDefaultValues() {
        RabbitMqLocalServiceProperties properties = createProperties();
        assertThat(properties.getManagementConsolePort()).isEqualTo(0);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        RabbitMqLocalServiceProperties properties = createProperties();
        properties.setManagementConsolePort(RoseRabbitMqContainer.HTTP_PORT);
        assertThat(properties.getManagementConsolePort()).isEqualTo(RoseRabbitMqContainer.HTTP_PORT);
    }

}
