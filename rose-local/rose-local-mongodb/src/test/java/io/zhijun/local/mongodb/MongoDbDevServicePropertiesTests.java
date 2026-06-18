package io.zhijun.local.mongodb;

import io.zhijun.local.tests.BaseLocalServicePropertiesTests;

/**
 * Unit tests for {@link MongoDbLocalServiceProperties}.
 */
class MongoDbDevServicePropertiesTests extends BaseLocalServicePropertiesTests<MongoDbLocalServiceProperties> {

    @Override
    protected MongoDbLocalServiceProperties createProperties() {
        return new MongoDbLocalServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseMongoDbContainer.COMPATIBLE_IMAGE_NAME)
                .build();
    }

}
