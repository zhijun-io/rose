package io.zhijun.dev.mongodb;

import io.zhijun.dev.tests.BaseDevServicePropertiesTests;

/**
 * Unit tests for {@link MongoDbDevServiceProperties}.
 */
class MongoDbDevServicePropertiesTests extends BaseDevServicePropertiesTests<MongoDbDevServiceProperties> {

    @Override
    protected MongoDbDevServiceProperties createProperties() {
        return new MongoDbDevServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseMongoDbContainer.COMPATIBLE_IMAGE_NAME)
                .build();
    }

}
