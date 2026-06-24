package io.zhijun.devservice.boot.autoconfigure.mysql;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

import io.zhijun.devservice.test.BaseJdbcDevServiceAutoConfigurationIT;

import static io.zhijun.devservice.boot.autoconfigure.mysql.MySqlDevServiceProperties.DEFAULT_DB_NAME;
import static io.zhijun.devservice.boot.autoconfigure.mysql.MySqlDevServiceProperties.DEFAULT_PASSWORD;
import static io.zhijun.devservice.boot.autoconfigure.mysql.MySqlDevServiceProperties.DEFAULT_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link MySqlDevServicesAutoConfiguration}.
 */
class MySqlDevServiceAutoConfigurationIT extends BaseJdbcDevServiceAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(MySqlDevServicesAutoConfiguration.class);

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return MySqlDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<? extends JdbcDatabaseContainer> getJdbcContainerClass() {
        return MySQLContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "mysql";
    }

    @Test
    void containerAvailableWithDefaultConfiguration() {
        getContextRunner().run(context -> {
            assertThat(context).hasSingleBean(getContainerClass());
            JdbcDatabaseContainer container = context.getBean(getJdbcContainerClass());
            assertThat(container.getDockerImageName()).contains(RoseMySqlContainer.COMPATIBLE_IMAGE_NAME);
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
        String[] properties = ArrayUtils.addAll(
                commonConfigurationProperties(), commonJdbcConfigurationProperties());

        getContextRunner()
                .withPropertyValues(properties)
                .run(context -> {
                    JdbcDatabaseContainer container = context.getBean(getJdbcContainerClass());
                    container.start();
                    assertThatConfigurationIsApplied((org.testcontainers.containers.GenericContainer<?>) container);
                    assertThatJdbcConfigurationIsApplied(container);
                    assertThat(((MySQLContainer<?>) container).execInContainer(
                                    "mysql", "-u", "mytest", "-pmytest", "mytest", "-N", "-e",
                                    "SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables "
                                            + "WHERE table_schema = 'mytest' AND table_name = 'BOOK'), 'true', 'false')")
                            .getStdout())
                            .contains("true");
                    container.stop();
                });
    }
}
