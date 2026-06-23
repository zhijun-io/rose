package io.zhijun.dev.tests;

import org.testcontainers.containers.JdbcDatabaseContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base JDBC dev services integration tests.
 */
public abstract class BaseJdbcDevServiceAutoConfigurationIT extends BaseDevServiceAutoConfigurationIT {

    protected abstract Class<? extends JdbcDatabaseContainer> getJdbcContainerClass();

    @Override
    protected Class<?> getContainerClass() {
        return getJdbcContainerClass();
    }

    protected String[] commonJdbcConfigurationProperties() {
        String prefix = "rose.local." + getServiceName();
        return new String[] {
                prefix + ".username=mytest",
                prefix + ".password=mytest",
                prefix + ".db-name=mytest",
                prefix + ".init-script-paths=sql/init.sql"
        };
    }

    protected void assertThatJdbcConfigurationIsApplied(JdbcDatabaseContainer container) {
        assertThat(container.getUsername()).isEqualTo("mytest");
        assertThat(container.getPassword()).isEqualTo("mytest");
        assertThat(container.getDatabaseName()).isEqualTo("mytest");
    }
}
