package io.zhijun.dev.services.mysql;

import io.zhijun.dev.services.tests.BaseJdbcDevServicesPropertiesTests;

/**
 * Unit tests for {@link MySqlDevServicesProperties}.
 */
class MySqlDevServicesPropertiesTests extends BaseJdbcDevServicesPropertiesTests<MySqlDevServicesProperties> {

    @Override
    protected MySqlDevServicesProperties createProperties() {
        return new MySqlDevServicesProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseMySqlContainer.COMPATIBLE_IMAGE_NAME)
                .build();
    }

}
