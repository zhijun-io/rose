package io.zhijun.local.mqtt;

import java.time.Duration;

import io.zhijun.local.tests.BaseLocalServicePropertiesTests;

/**
 * Unit tests for {@link MqttLocalServiceProperties}.
 */
class MqttDevServicePropertiesTests extends BaseLocalServicePropertiesTests<MqttLocalServiceProperties> {

    @Override
    protected MqttLocalServiceProperties createProperties() {
        return new MqttLocalServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseHiveMQContainer.COMPATIBLE_IMAGE_NAME)
                .startupTimeout(Duration.ofSeconds(60))
                .build();
    }

}
