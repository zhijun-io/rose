package io.zhijun.devservice.test;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.core.api.config.JdbcDevServiceProperties;

import static io.zhijun.devservice.core.api.config.JdbcDevServiceProperties.DEFAULT_DB_NAME;
import static io.zhijun.devservice.core.api.config.JdbcDevServiceProperties.DEFAULT_PASSWORD;
import static io.zhijun.devservice.core.api.config.JdbcDevServiceProperties.DEFAULT_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base test class for testing {@link JdbcDevServiceProperties} implementations.
 *
 * @param <T> the specific {@link JdbcDevServiceProperties} implementation type
 */
public abstract class BaseJdbcDevServicePropertiesTests<T extends JdbcDevServiceProperties>
        extends BaseDevServicePropertiesTests<T> {

    @Test
    void shouldCreateInstanceWithJdbcDefaultValues() {
        T properties = createProperties();

        assertThat(properties.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(DEFAULT_PASSWORD);
        assertThat(properties.getDbName()).isEqualTo(DEFAULT_DB_NAME);
        assertThat(properties.getInitScriptPaths()).isEmpty();
    }

    @Test
    void shouldUpdateCommonJdbcProperties() {
        T properties = createProperties();

        properties.setUsername("mytest");
        properties.setPassword("mytest");
        properties.setDbName("mytest");
        properties.setInitScriptPaths(Collections.singletonList("init.sql"));

        assertThat(properties.getUsername()).isEqualTo("mytest");
        assertThat(properties.getPassword()).isEqualTo("mytest");
        assertThat(properties.getDbName()).isEqualTo("mytest");
        assertThat(properties.getInitScriptPaths()).containsExactly("init.sql");
    }
}
