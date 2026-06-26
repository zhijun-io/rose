package io.zhijun.devservice.core.api.config;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link JdbcDevServiceProperties}.
 */
class JdbcDevServicePropertiesTests {

    @Test
    void defaultsAndSetters() {
        JdbcDevServiceProperties properties = new JdbcDevServiceProperties() {
        };

        assertThat(properties.getUsername()).isEqualTo(DevServiceCredentials.DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(DevServiceCredentials.DEFAULT_PASSWORD);
        assertThat(properties.getDbName()).isEqualTo(DevServiceCredentials.DEFAULT_DB_NAME);
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
