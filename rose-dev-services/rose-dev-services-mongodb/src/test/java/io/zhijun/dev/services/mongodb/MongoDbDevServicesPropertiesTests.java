package io.zhijun.dev.services.mongodb;

import io.zhijun.dev.services.tests.BaseDevServicesPropertiesTests;

/**
 * Unit tests for {@link MongoDbDevServicesProperties}.
 */
class MongoDbDevServicesPropertiesTests extends BaseDevServicesPropertiesTests<MongoDbDevServicesProperties> {

    @Override
    protected MongoDbDevServicesProperties createProperties() {
        return new MongoDbDevServicesProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseMongoDbContainer.COMPATIBLE_IMAGE_NAME)
                .build();
    }

}
