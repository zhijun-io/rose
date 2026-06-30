package io.zhijun.devservice.boot.autoconfigure.postgresql;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.DevServiceAutoConfigRegistrar;
import io.zhijun.devservice.boot.registration.JdbcDevServiceConnectorDescriptor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * PostgreSQL dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled(PostgresqlDevServiceProperties.SERVICE_NAME)
@EnableConfigurationProperties(PostgresqlDevServiceProperties.class)
@Import(DevServiceAutoConfigRegistrar.class)
public final class PostgresqlDevServicesAutoConfiguration {

    static final JdbcDevServiceConnectorDescriptor<PostgresqlDevServiceProperties, PostgresqlContainer>
            DESCRIPTOR =
                    JdbcDevServiceConnectorDescriptor.<PostgresqlDevServiceProperties, PostgresqlContainer>builder()
                            .propertiesType(PostgresqlDevServiceProperties.class)
                            .configPrefix(PostgresqlDevServiceProperties.CONFIG_PREFIX)
                            .serviceName(PostgresqlDevServiceProperties.SERVICE_NAME)
                            .displayName("PostgreSQL Dev Service")
                            .containerClass(PostgresqlContainer.class)
                            .containerFactory(PostgresqlContainer::new)
                            .build();
}
