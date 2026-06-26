package io.zhijun.devservice.boot.autoconfigure.mongodb;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
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
                .imageName(MongoDbDevServiceProperties.DEFAULT_IMAGE_NAME)
                .build();
    }

}
