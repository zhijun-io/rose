package io.zhijun.devservice.boot.autoconfigure.postgresql;

import static io.zhijun.devservice.core.api.config.DevServiceCredentials.DEFAULT_DB_NAME;
import static io.zhijun.devservice.core.api.config.DevServiceCredentials.DEFAULT_PASSWORD;
import static io.zhijun.devservice.core.api.config.DevServiceCredentials.DEFAULT_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import io.zhijun.devservice.test.BaseJdbcDevServiceAutoConfigurationIT;

/**
 * Integration test for {@link PostgresqlDevServicesAutoConfiguration}.
 */
class PostgresqlDevServiceAutoConfigurationIT extends BaseJdbcDevServiceAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(
                    PostgresqlDevServicesAutoConfiguration.class)
            .withClassLoader(new FilteredClassLoader(RestartScope.class));

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return PostgresqlDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<? extends JdbcDatabaseContainer> getJdbcContainerClass() {
        return PostgreSQLContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "postgresql";
    }

    @Test
    void containerAvailableWithDefaultConfiguration() {
        getContextRunner().run(context -> {
            assertThat(context).hasSingleBean(getContainerClass());
            JdbcDatabaseContainer container = context.getBean(getJdbcContainerClass());
            assertThat(container.getDockerImageName()).contains(PostgresqlContainer.COMPATIBLE_IMAGE_NAME);
            assertThat(container.getEnv()).isEmpty();
            assertThat(container.getNetworkAliases()).hasSize(1);
            assertThat(container.isShouldBeReused()).isFalse();
            container.start();
            assertThat(container.getUsername()).isEqualTo(DEFAULT_USERNAME);
            assertThat(container.getPassword()).isEqualTo(DEFAULT_PASSWORD);
            assertThat(container.getDatabaseName()).isEqualTo(DEFAULT_DB_NAME);
            container.stop();

            assertThatHasSingletonScope(context);
        });
    }

    @Test
    void containerConfigurationApplied() {
        String[] properties = ArrayUtils.addAll(commonConfigurationProperties(), commonJdbcConfigurationProperties());

        getContextRunner().withPropertyValues(properties).run(context -> {
            JdbcDatabaseContainer container = context.getBean(getJdbcContainerClass());
            container.start();
            assertThatConfigurationIsApplied((org.testcontainers.containers.GenericContainer<?>) container);
            assertThatJdbcConfigurationIsApplied(container);
            assertThat(((PostgreSQLContainer) container)
                            .execInContainer(
                                    "psql",
                                    "-U",
                                    "mytest",
                                    "-d",
                                    "mytest",
                                    "-t",
                                    "-A",
                                    "-c",
                                    "SELECT EXISTS (SELECT FROM pg_tables WHERE tablename = 'book')::text")
                            .getStdout())
                    .contains("true");
            container.stop();
        });
    }

    @Test
    void defaultPostgresImageConfigured() {
        getContextRunner().run(context -> {
            JdbcDatabaseContainer container = context.getBean(getJdbcContainerClass());
            assertThat(container.getDockerImageName()).contains("postgres");
        });
    }
}
