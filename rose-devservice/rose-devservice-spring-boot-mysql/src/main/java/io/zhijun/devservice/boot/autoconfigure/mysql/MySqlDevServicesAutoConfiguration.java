package io.zhijun.devservice.boot.autoconfigure.mysql;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.JdbcDevServiceRegistrar;
import io.zhijun.devservice.boot.autoconfigure.mysql.MySqlDevServicesAutoConfiguration.MySqlDevServiceRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MySQL dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("mysql")
@EnableConfigurationProperties(MySqlDevServiceProperties.class)
@Import(MySqlDevServiceRegistrar.class)
public final class MySqlDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider mySqlDevServiceProvider() {
        return DevServiceProvider.of("mysql", DevServiceCategories.JDBC);
    }

    static class MySqlDevServiceRegistrar
            extends JdbcDevServiceRegistrar<MySqlDevServiceProperties, RoseMySqlContainer> {

        @Override
        protected Class<MySqlDevServiceProperties> getPropertiesType() {
            return MySqlDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return MySqlDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "mysql";
        }

        @Override
        protected String getDisplayName() {
            return "MySQL Dev Service";
        }

        @Override
        protected Class<RoseMySqlContainer> getContainerClass() {
            return RoseMySqlContainer.class;
        }

        @Override
        protected RoseMySqlContainer createContainer(MySqlDevServiceProperties properties) {
            return new RoseMySqlContainer(properties);
        }
    }
}
