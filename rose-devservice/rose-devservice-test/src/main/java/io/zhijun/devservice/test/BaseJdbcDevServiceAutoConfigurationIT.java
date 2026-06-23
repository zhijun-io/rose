package io.zhijun.devservice.test;

import org.testcontainers.containers.JdbcDatabaseContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base JDBC dev services integration test.
 */
public abstract class BaseJdbcDevServiceAutoConfigurationIT extends BaseDevServiceAutoConfigurationIT {

    protected abstract Class<? extends JdbcDatabaseContainer> getJdbcContainerClass();

    @Override
    protected Class<?> getContainerClass() {
        return getJdbcContainerClass();
    }

    protected String[] commonJdbcConfigurationProperties() {
        String prefix = "rose.dev." + getServiceName();
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
