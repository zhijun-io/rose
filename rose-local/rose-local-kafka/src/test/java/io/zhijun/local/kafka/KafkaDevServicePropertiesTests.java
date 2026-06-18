package io.zhijun.local.kafka;

import io.zhijun.local.tests.BaseLocalServicePropertiesTests;

/**
 * Unit tests for {@link KafkaLocalServiceProperties}.
 */
class KafkaDevServicePropertiesTests extends BaseLocalServicePropertiesTests<KafkaLocalServiceProperties> {

    @Override
    protected KafkaLocalServiceProperties createProperties() {
        return new KafkaLocalServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseKafkaContainer.COMPATIBLE_IMAGE_NAME)
                .shared(true)
                .build();
    }

}
