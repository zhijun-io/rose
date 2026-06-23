package io.zhijun.dev.kafka;

import io.zhijun.dev.test.BaseDevServicePropertiesTests;

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
                .imageName(RoseKafkaContainer.COMPATIBLE_IMAGE_NAME)
                .shared(true)
                .build();
    }

}
