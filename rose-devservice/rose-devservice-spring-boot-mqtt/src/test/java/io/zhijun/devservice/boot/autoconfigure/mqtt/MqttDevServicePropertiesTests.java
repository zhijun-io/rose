package io.zhijun.devservice.boot.autoconfigure.mqtt;

import java.time.Duration;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.test.BaseDevServicePropertiesTests;

/**
 * Unit test for {@link MqttDevServiceProperties}.
 */
class MqttDevServicePropertiesTests extends BaseDevServicePropertiesTests<MqttDevServiceProperties> {

    @Override
    protected MqttDevServiceProperties createProperties() {
        return new MqttDevServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(MqttDevServiceProperties.DEFAULT_IMAGE_NAME)
                .startupTimeout(BaseDevServiceProperties.SLOW_STARTUP_TIMEOUT)
                .build();
    }

}
