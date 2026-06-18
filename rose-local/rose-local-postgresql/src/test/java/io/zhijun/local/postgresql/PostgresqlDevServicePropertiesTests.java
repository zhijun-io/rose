package io.zhijun.local.postgresql;

import io.zhijun.local.tests.BaseJdbcDevServicePropertiesTests;

/**
 * Unit tests for {@link PostgresqlLocalServiceProperties}.
 */
class PostgresqlDevServicePropertiesTests extends BaseJdbcDevServicePropertiesTests<PostgresqlLocalServiceProperties> {

    @Override
    protected PostgresqlLocalServiceProperties createProperties() {
        return new PostgresqlLocalServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName("postgres")
                .build();
    }

}
