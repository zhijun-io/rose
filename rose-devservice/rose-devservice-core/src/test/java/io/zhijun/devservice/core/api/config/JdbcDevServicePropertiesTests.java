package io.zhijun.devservice.core.api.config;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link JdbcDevServiceProperties}.
 */
class JdbcDevServicePropertiesTests {

    @Test
    void constants() {
        assertThat(JdbcDevServiceProperties.DEFAULT_USERNAME).isEqualTo("rose");
        assertThat(JdbcDevServiceProperties.DEFAULT_PASSWORD).isEqualTo("rose");
        assertThat(JdbcDevServiceProperties.DEFAULT_DB_NAME).isEqualTo("rose");
    }

    @Test
    void defaultsAndSetters() {
        JdbcDevServiceProperties properties = new JdbcDevServiceProperties() {
        };

        assertThat(properties.getUsername()).isEqualTo(JdbcDevServiceProperties.DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(JdbcDevServiceProperties.DEFAULT_PASSWORD);
        assertThat(properties.getDbName()).isEqualTo(JdbcDevServiceProperties.DEFAULT_DB_NAME);
        assertThat(properties.getInitScriptPaths()).isEmpty();

        properties.setUsername("other");
        properties.setPassword("secret");
        properties.setDbName("other-db");
        properties.setInitScriptPaths(Arrays.asList("other.sql"));

        assertThat(properties.getUsername()).isEqualTo("other");
        assertThat(properties.getPassword()).isEqualTo("secret");
        assertThat(properties.getDbName()).isEqualTo("other-db");
        assertThat(properties.getInitScriptPaths()).containsExactly("other.sql");
    }

}
