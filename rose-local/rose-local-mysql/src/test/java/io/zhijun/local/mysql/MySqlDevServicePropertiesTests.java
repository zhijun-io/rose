package io.zhijun.local.mysql;

import io.zhijun.local.tests.BaseJdbcDevServicePropertiesTests;

/**
 * Unit tests for {@link MySqlLocalServiceProperties}.
 */
class MySqlDevServicePropertiesTests extends BaseJdbcDevServicePropertiesTests<MySqlLocalServiceProperties> {

    @Override
    protected MySqlLocalServiceProperties createProperties() {
        return new MySqlLocalServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseMySqlContainer.COMPATIBLE_IMAGE_NAME)
                .build();
    }

}
