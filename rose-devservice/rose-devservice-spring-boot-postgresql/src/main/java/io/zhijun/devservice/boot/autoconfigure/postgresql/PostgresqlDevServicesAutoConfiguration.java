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

/**
 * PostgreSQL dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled(PostgresqlDevServiceProperties.SERVICE_NAME)
@EnableConfigurationProperties(PostgresqlDevServiceProperties.class)
@Import(PostgresqlDevServicesAutoConfiguration.PostgresqlDevServiceRegistrar.class)
public final class PostgresqlDevServicesAutoConfiguration {

    private static final JdbcDevServiceConnectorDescriptor<PostgresqlDevServiceProperties, PostgresqlContainer>
            DESCRIPTOR =
                    JdbcDevServiceConnectorDescriptor.<PostgresqlDevServiceProperties, PostgresqlContainer>builder()
                            .propertiesType(PostgresqlDevServiceProperties.class)
                            .configPrefix(PostgresqlDevServiceProperties.CONFIG_PREFIX)
                            .serviceName(PostgresqlDevServiceProperties.SERVICE_NAME)
                            .displayName("PostgreSQL Dev Service")
                            .containerClass(PostgresqlContainer.class)
                            .containerFactory(PostgresqlContainer::new)
                            .build();

    static final class PostgresqlDevServiceRegistrar
            extends JdbcDevServiceRegistrar<PostgresqlDevServiceProperties, PostgresqlContainer> {

        PostgresqlDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
