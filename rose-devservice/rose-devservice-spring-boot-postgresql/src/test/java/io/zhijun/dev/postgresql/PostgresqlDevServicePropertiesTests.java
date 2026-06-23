package io.zhijun.dev.postgresql;

import io.zhijun.dev.test.BaseJdbcDevServicePropertiesTests;

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
                .imageName("postgres")
                .build();
    }

}
