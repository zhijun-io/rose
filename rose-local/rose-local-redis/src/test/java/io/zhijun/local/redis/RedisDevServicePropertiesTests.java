package io.zhijun.local.redis;

import io.zhijun.local.tests.BaseLocalServicePropertiesTests;

/**
 * Unit tests for {@link RedisLocalServiceProperties}.
 */
class RedisDevServicePropertiesTests extends BaseLocalServicePropertiesTests<RedisLocalServiceProperties> {

    @Override
    protected RedisLocalServiceProperties createProperties() {
        return new RedisLocalServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseRedisContainer.COMPATIBLE_IMAGE_NAME)
                .build();
    }

}
