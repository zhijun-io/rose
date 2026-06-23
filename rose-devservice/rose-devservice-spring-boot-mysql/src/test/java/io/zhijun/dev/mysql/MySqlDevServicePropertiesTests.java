package io.zhijun.dev.mysql;

import io.zhijun.dev.test.BaseJdbcDevServicePropertiesTests;

/**
 * Unit test for {@link MySqlDevServiceProperties}.
 */
class MySqlDevServicePropertiesTests extends BaseJdbcDevServicePropertiesTests<MySqlDevServiceProperties> {

    @Override
    protected MySqlDevServiceProperties createProperties() {
        return new MySqlDevServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseMySqlContainer.COMPATIBLE_IMAGE_NAME)
                .build();
    }

}
