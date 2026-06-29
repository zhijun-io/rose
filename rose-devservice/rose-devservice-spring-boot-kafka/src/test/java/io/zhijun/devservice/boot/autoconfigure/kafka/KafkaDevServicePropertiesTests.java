package io.zhijun.devservice.boot.autoconfigure.kafka;

import io.zhijun.devservice.test.BaseDevServicePropertiesTests;

/**
 * Unit test for {@link KafkaDevServiceProperties}.
 */
class KafkaDevServicePropertiesTests extends BaseDevServicePropertiesTests<KafkaDevServiceProperties> {

    @Override
    protected KafkaDevServiceProperties createProperties() {
        return new KafkaDevServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(KafkaDevServiceProperties.DEFAULT_IMAGE_NAME)
                .shared(true)
                .build();
    }
}
