package io.zhijun.devservice.boot.autoconfigure.postgresql;

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

    static class PostgresqlDevServiceRegistrar
            extends JdbcDevServiceRegistrar<PostgresqlDevServiceProperties, RosePostgreSqlContainer> {

        @Override
        protected Class<PostgresqlDevServiceProperties> getPropertiesType() {
            return PostgresqlDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return PostgresqlDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "postgresql";
        }

        @Override
        protected String getDisplayName() {
            return "PostgreSQL Dev Service";
        }

        @Override
        protected Class<RosePostgreSqlContainer> getContainerClass() {
            return RosePostgreSqlContainer.class;
        }

        @Override
        protected RosePostgreSqlContainer createContainer(PostgresqlDevServiceProperties properties) {
            return new RosePostgreSqlContainer(properties);
        }
    }
}
