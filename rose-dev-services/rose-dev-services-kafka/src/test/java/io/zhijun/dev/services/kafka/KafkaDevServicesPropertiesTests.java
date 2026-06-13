package io.zhijun.dev.services.kafka;

import io.zhijun.dev.services.tests.BaseDevServicesPropertiesTests;

/**
 * Unit tests for {@link KafkaDevServicesProperties}.
 */
class KafkaDevServicesPropertiesTests extends BaseDevServicesPropertiesTests<KafkaDevServicesProperties> {

    @Override
    protected KafkaDevServicesProperties createProperties() {
        return new KafkaDevServicesProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseKafkaContainer.COMPATIBLE_IMAGE_NAME)
                .shared(true)
                .build();
    }

}
