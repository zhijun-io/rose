package io.zhijun.devservice.boot.autoconfigure.postgresql;

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
import io.zhijun.devservice.boot.autoconfigure.postgresql.PostgresqlDevServicesAutoConfiguration.PostgresqlDevServiceRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * PostgreSQL dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("postgresql")
@EnableConfigurationProperties(PostgresqlDevServiceProperties.class)
@Import(PostgresqlDevServiceRegistrar.class)
public final class PostgresqlDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider postgresqlDevServiceProvider() {
        return DevServiceProvider.of("postgresql", DevServiceCategories.JDBC);
    }

    static class PostgresqlDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            PostgresqlDevServiceProperties properties = bindProperties(
                    PostgresqlDevServiceProperties.CONFIG_PREFIX, PostgresqlDevServiceProperties.class);

            registry.registerDevService("postgresql", "PostgreSQL Dev Service",
                    RosePostgreSqlContainer.class, () -> new RosePostgreSqlContainer(properties));

            addDynamicProperty("spring.datasource.url", () -> jdbcContainer().getJdbcUrl());
            addDynamicProperty("spring.datasource.username", () -> jdbcContainer().getUsername());
            addDynamicProperty("spring.datasource.password", () -> jdbcContainer().getPassword());
        }

        private JdbcDatabaseContainer<?> jdbcContainer() {
            RosePostgreSqlContainer container = getBeanFactory().getBean(RosePostgreSqlContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
