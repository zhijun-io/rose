package io.zhijun.devservice.boot.autoconfigure.redis;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.test.BaseDevServicePropertiesTests;

/**
 * Unit test for {@link RedisDevServiceProperties}.
 */
class RedisDevServicePropertiesTests extends BaseDevServicePropertiesTests<RedisDevServiceProperties> {

    @Override
    protected RedisDevServiceProperties createProperties() {
        return new RedisDevServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RedisDevServiceProperties.DEFAULT_IMAGE_NAME)
                .shared(true)
                .build();
    }

}
