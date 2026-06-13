package io.zhijun.dev.services.redis;

import io.zhijun.dev.services.tests.BaseDevServicesPropertiesTests;

/**
 * Unit tests for {@link RedisDevServicesProperties}.
 */
class RedisDevServicesPropertiesTests extends BaseDevServicesPropertiesTests<RedisDevServicesProperties> {

    @Override
    protected RedisDevServicesProperties createProperties() {
        return new RedisDevServicesProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseRedisContainer.COMPATIBLE_IMAGE_NAME)
                .build();
    }

}
