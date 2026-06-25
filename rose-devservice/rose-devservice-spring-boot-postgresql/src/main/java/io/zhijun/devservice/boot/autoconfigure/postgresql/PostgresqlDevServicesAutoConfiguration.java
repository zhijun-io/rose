package io.zhijun.devservice.boot.autoconfigure.postgresql;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.JdbcDevServiceConnectorDescriptor;
import io.zhijun.devservice.boot.registration.JdbcDevServiceRegistrar;
import io.zhijun.devservice.core.api.provider.DevServiceCategories;

/**
 * PostgreSQL dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("postgresql")
@EnableConfigurationProperties(PostgresqlDevServiceProperties.class)
@Import(PostgresqlDevServicesAutoConfiguration.PostgresqlDevServiceRegistrar.class)
public final class PostgresqlDevServicesAutoConfiguration {

    private static final JdbcDevServiceConnectorDescriptor<PostgresqlDevServiceProperties, RosePostgreSqlContainer> DESCRIPTOR =
            JdbcDevServiceConnectorDescriptor.<PostgresqlDevServiceProperties, RosePostgreSqlContainer>builder()
                    .propertiesType(PostgresqlDevServiceProperties.class)
                    .configPrefix(PostgresqlDevServiceProperties.CONFIG_PREFIX)
                    .serviceName("postgresql")
                    .displayName("PostgreSQL Dev Service")
                    .category(DevServiceCategories.JDBC)
                    .containerClass(RosePostgreSqlContainer.class)
                    .containerFactory(RosePostgreSqlContainer::new)
                    .build();

    static final class PostgresqlDevServiceRegistrar
            extends JdbcDevServiceRegistrar<PostgresqlDevServiceProperties, RosePostgreSqlContainer> {

        PostgresqlDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
