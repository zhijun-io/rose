package io.zhijun.devservice.boot.autoconfigure.mongodb;

import io.zhijun.devservice.test.BaseDevServicePropertiesTests;

/**
 * Unit test for {@link MongoDbDevServiceProperties}.
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
