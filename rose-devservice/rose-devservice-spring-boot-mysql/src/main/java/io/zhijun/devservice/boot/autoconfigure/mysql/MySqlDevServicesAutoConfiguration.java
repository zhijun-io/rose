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
import io.zhijun.devservice.core.api.provider.DevServiceCategories;

/**
 * MySQL dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("mysql")
@EnableConfigurationProperties(MySqlDevServiceProperties.class)
@Import(MySqlDevServicesAutoConfiguration.MySqlDevServiceRegistrar.class)
public final class MySqlDevServicesAutoConfiguration {

    private static final JdbcDevServiceConnectorDescriptor<MySqlDevServiceProperties, RoseMySqlContainer> DESCRIPTOR =
            JdbcDevServiceConnectorDescriptor.<MySqlDevServiceProperties, RoseMySqlContainer>builder()
                    .propertiesType(MySqlDevServiceProperties.class)
                    .configPrefix(MySqlDevServiceProperties.CONFIG_PREFIX)
                    .serviceName("mysql")
                    .displayName("MySQL Dev Service")
                    .category(DevServiceCategories.JDBC)
                    .containerClass(RoseMySqlContainer.class)
                    .containerFactory(RoseMySqlContainer::new)
                    .build();

    static final class MySqlDevServiceRegistrar
            extends JdbcDevServiceRegistrar<MySqlDevServiceProperties, RoseMySqlContainer> {

        MySqlDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
