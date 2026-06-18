package io.zhijun.dev.services.postgresql;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import io.zhijun.dev.services.api.provider.DevServiceCategories;
import io.zhijun.dev.services.api.provider.DevServiceProvider;
import io.zhijun.dev.services.core.autoconfigure.ConditionalOnDevServicesEnabled;
import io.zhijun.dev.services.core.autoconfigure.DevServicesAutoConfiguration;
import io.zhijun.dev.services.core.registration.DevServicesRegistrar;
import io.zhijun.dev.services.core.registration.DevServicesRegistry;
import io.zhijun.dev.services.postgresql.PostgresqlDevServicesAutoConfiguration.PostgresqlDevServicesRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * PostgreSQL dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnDevServicesEnabled("postgresql")
@EnableConfigurationProperties(PostgresqlDevServicesProperties.class)
@Import(PostgresqlDevServicesRegistrar.class)
public final class PostgresqlDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider postgresqlDevServiceProvider() {
        return DevServiceProvider.of("postgresql", DevServiceCategories.JDBC);
    }

    static class PostgresqlDevServicesRegistrar extends DevServicesRegistrar {

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final PostgresqlDevServicesProperties properties = bindProperties(
                    PostgresqlDevServicesProperties.CONFIG_PREFIX, PostgresqlDevServicesProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServicesRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServicesRegistry.ServiceSpec service) {
                    service
                            .name("postgresql")
                            .description("PostgreSQL Dev Service")
                            .container(new java.util.function.Consumer<DevServicesRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServicesRegistry.ContainerSpec container) {
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
