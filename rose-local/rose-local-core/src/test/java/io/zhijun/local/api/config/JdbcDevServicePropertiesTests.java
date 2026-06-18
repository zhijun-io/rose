package io.zhijun.local.api.config;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JdbcLocalServiceProperties}.
 */
class JdbcDevServicePropertiesTests {

  @Test
  void constants() {
    assertThat(JdbcLocalServiceProperties.DEFAULT_USERNAME).isEqualTo("rose");
    assertThat(JdbcLocalServiceProperties.DEFAULT_PASSWORD).isEqualTo("rose");
    assertThat(JdbcLocalServiceProperties.DEFAULT_DB_NAME).isEqualTo("rose");
  }

  @Test
  void defaultSettersAreNoOps() {
    JdbcLocalServiceProperties properties = new JdbcLocalServiceProperties() {
      @Override
      public String getImageName() {
        return "postgres:16";
      }

      @Override
      public void setImageName(String imageName) {
      }

      @Override
      public String getUsername() {
        return "user";
      }

      @Override
      public String getPassword() {
        return "pass";
      }

      @Override
      public String getDbName() {
        return "db";
      }

      @Override
      public List<String> getInitScriptPaths() {
        return Arrays.asList("init.sql");
      }
    };

    properties.setUsername("other");
    properties.setPassword("secret");
    properties.setDbName("other-db");
    properties.setInitScriptPaths(Arrays.asList("other.sql"));

    assertThat(properties.getUsername()).isEqualTo("user");
    assertThat(properties.getPassword()).isEqualTo("pass");
    assertThat(properties.getDbName()).isEqualTo("db");
    assertThat(properties.getInitScriptPaths()).containsExactly("init.sql");
  }

}
