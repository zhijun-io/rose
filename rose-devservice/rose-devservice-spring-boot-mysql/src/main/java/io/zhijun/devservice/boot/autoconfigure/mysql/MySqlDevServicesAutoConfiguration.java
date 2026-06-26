package io.zhijun.devservice.boot.autoconfigure.mysql;

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
 * MySQL dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled(MySqlDevServiceProperties.SERVICE_NAME)
@EnableConfigurationProperties(MySqlDevServiceProperties.class)
@Import(MySqlDevServicesAutoConfiguration.MySqlDevServiceRegistrar.class)
public final class MySqlDevServicesAutoConfiguration {

    private static final JdbcDevServiceConnectorDescriptor<MySqlDevServiceProperties, MySqlContainer> DESCRIPTOR =
            JdbcDevServiceConnectorDescriptor.<MySqlDevServiceProperties, MySqlContainer>builder()
                    .propertiesType(MySqlDevServiceProperties.class)
                    .configPrefix(MySqlDevServiceProperties.CONFIG_PREFIX)
                    .serviceName(MySqlDevServiceProperties.SERVICE_NAME)
                    .displayName("MySQL Dev Service")
                    .containerClass(MySqlContainer.class)
                    .containerFactory(MySqlContainer::new)
                    .build();

    static final class MySqlDevServiceRegistrar
            extends JdbcDevServiceRegistrar<MySqlDevServiceProperties, MySqlContainer> {

        MySqlDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
