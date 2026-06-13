package io.zhijun.dev.services.mysql;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

import io.zhijun.dev.services.api.provider.DevServiceCategories;
import io.zhijun.dev.services.api.provider.DevServiceProvider;
import io.zhijun.dev.services.core.autoconfigure.ConditionalOnDevServicesEnabled;
import io.zhijun.dev.services.core.autoconfigure.DevServicesAutoConfiguration;
import io.zhijun.dev.services.core.registration.DevServicesRegistrar;
import io.zhijun.dev.services.core.registration.DevServicesRegistry;
import io.zhijun.dev.services.mysql.MySqlDevServicesAutoConfiguration.MySqlDevServicesRegistrar;

/**
 * MySQL dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnDevServicesEnabled("mysql")
@EnableConfigurationProperties(MySqlDevServicesProperties.class)
@Import(MySqlDevServicesRegistrar.class)
public final class MySqlDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider mySqlDevServiceProvider() {
        return DevServiceProvider.of("mysql", DevServiceCategories.JDBC);
    }

    static class MySqlDevServicesRegistrar extends DevServicesRegistrar {

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final MySqlDevServicesProperties properties = bindProperties(
                    MySqlDevServicesProperties.CONFIG_PREFIX, MySqlDevServicesProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServicesRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServicesRegistry.ServiceSpec service) {
                    service
                            .name("mysql")
                            .description("MySQL Dev Service")
                            .container(new java.util.function.Consumer<DevServicesRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServicesRegistry.ContainerSpec container) {
                                    container
                                            .type(RoseMySqlContainer.class)
                                            .supplier(new java.util.function.Supplier<org.testcontainers.containers.Container<?>>() {
                                                @Override
                                                public org.testcontainers.containers.Container<?> get() {
                                                    return new RoseMySqlContainer(properties);
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
            MySQLContainer<?> container = getBeanFactory().getBean(RoseMySqlContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
