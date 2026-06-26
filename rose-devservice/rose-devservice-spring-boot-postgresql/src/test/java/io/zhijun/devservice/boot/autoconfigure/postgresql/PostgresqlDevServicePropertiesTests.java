package io.zhijun.devservice.boot.autoconfigure.postgresql;

import io.zhijun.devservice.test.BaseJdbcDevServicePropertiesTests;

/**
 * Unit test for {@link PostgresqlDevServiceProperties}.
 */
class PostgresqlDevServicePropertiesTests extends BaseJdbcDevServicePropertiesTests<PostgresqlDevServiceProperties> {

    @Override
    protected PostgresqlDevServiceProperties createProperties() {
        return new PostgresqlDevServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(PostgresqlDevServiceProperties.DEFAULT_IMAGE_NAME)
                .shared(true)
                .build();
    }

}
