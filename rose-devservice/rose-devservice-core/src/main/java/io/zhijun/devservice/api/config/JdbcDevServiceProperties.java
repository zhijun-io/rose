package io.zhijun.devservice.api.config;

import java.util.List;

import io.zhijun.core.annotation.Incubating;

/**
 * JDBC dev service properties.
 */
@Incubating
public interface JdbcDevServiceProperties extends BaseDevServiceProperties {

    String DEFAULT_USERNAME = "rose";
    String DEFAULT_PASSWORD = "rose";
    String DEFAULT_DB_NAME = "rose";

    String getUsername();

    String getPassword();

    String getDbName();

    List<String> getInitScriptPaths();

    default void setUsername(String username) {
    }

    default void setPassword(String password) {
    }

    default void setDbName(String dbName) {
    }

    default void setInitScriptPaths(List<String> initScriptPaths) {
    }
}
