package io.zhijun.dev.services.mqtt;

import java.time.Duration;

import io.zhijun.dev.services.tests.BaseDevServicesPropertiesTests;

/**
 * Unit tests for {@link MqttDevServicesProperties}.
 */
class MqttDevServicesPropertiesTests extends BaseDevServicesPropertiesTests<MqttDevServicesProperties> {

    @Override
    protected MqttDevServicesProperties createProperties() {
        return new MqttDevServicesProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseHiveMQContainer.COMPATIBLE_IMAGE_NAME)
                .startupTimeout(Duration.ofSeconds(60))
                .build();
    }

}
