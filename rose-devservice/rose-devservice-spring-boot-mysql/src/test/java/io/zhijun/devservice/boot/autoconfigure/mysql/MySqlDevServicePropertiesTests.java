package io.zhijun.devservice.boot.autoconfigure.mysql;

import io.zhijun.devservice.test.BaseJdbcDevServicePropertiesTests;

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
                .imageName(MySqlDevServiceProperties.DEFAULT_IMAGE_NAME)
                .shared(true)
                .build();
    }
}
