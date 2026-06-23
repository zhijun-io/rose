package io.zhijun.dev.redis;

import io.zhijun.dev.test.BaseDevServicePropertiesTests;

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
                .imageName(RoseRedisContainer.COMPATIBLE_IMAGE_NAME)
                .build();
    }

}
