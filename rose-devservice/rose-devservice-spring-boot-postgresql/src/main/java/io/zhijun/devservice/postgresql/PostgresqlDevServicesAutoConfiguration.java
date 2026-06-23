package io.zhijun.devservice.postgresql;

import io.zhijun.devservice.core.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import io.zhijun.devservice.api.provider.DevServiceCategories;
import io.zhijun.devservice.api.provider.DevServiceProvider;
import io.zhijun.devservice.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.core.registration.DevServiceRegistrar;
import io.zhijun.devservice.core.registration.DevServiceRegistry;
import io.zhijun.devservice.postgresql.PostgresqlDevServicesAutoConfiguration.PostgresqlDevServiceRegistrar;
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
            final PostgresqlDevServiceProperties properties = bindProperties(
                    PostgresqlDevServiceProperties.CONFIG_PREFIX, PostgresqlDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServiceRegistry.ServiceSpec service) {
                    service
                            .name("postgresql")
                            .description("PostgreSQL Dev Service")
                            .container(new java.util.function.Consumer<DevServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServiceRegistry.ContainerSpec container) {
                                    container
                                            .type(RosePostgreSqlContainer.class)
                                            .supplier(new java.util.function.Supplier<org.testcontainers.containers.Container<?>>() {
                                                @Override
                                                public org.testcontainers.containers.Container<?> get() {
                                                    return new RosePostgreSqlContainer(properties);
                                                }
                                            });
                                }
                            });
                }
            });

            addDynamicProperty("spring.datasource.url", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return jdbcContainer().getJdbcUrl();
                }
            });
            addDynamicProperty("spring.datasource.username", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return jdbcContainer().getUsername();
                }
            });
            addDynamicProperty("spring.datasource.password", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return jdbcContainer().getPassword();
                }
            });
        }

        private JdbcDatabaseContainer<?> jdbcContainer() {
            PostgreSQLContainer<?> container = getBeanFactory().getBean(RosePostgreSqlContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
