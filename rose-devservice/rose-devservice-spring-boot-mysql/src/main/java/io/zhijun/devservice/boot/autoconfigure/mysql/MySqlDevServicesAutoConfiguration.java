package io.zhijun.devservice.boot.autoconfigure.mysql;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.JdbcDatabaseContainer;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.DevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceRegistry;
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

    static class MySqlDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            MySqlDevServiceProperties properties = bindProperties(
                    MySqlDevServiceProperties.CONFIG_PREFIX, MySqlDevServiceProperties.class);

            registry.registerDevService("mysql", "MySQL Dev Service",
                    RoseMySqlContainer.class, () -> new RoseMySqlContainer(properties));

            addDynamicProperty("spring.datasource.url", () -> jdbcContainer().getJdbcUrl());
            addDynamicProperty("spring.datasource.username", () -> jdbcContainer().getUsername());
            addDynamicProperty("spring.datasource.password", () -> jdbcContainer().getPassword());
        }

        private JdbcDatabaseContainer<?> jdbcContainer() {
            RoseMySqlContainer container = getBeanFactory().getBean(RoseMySqlContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
