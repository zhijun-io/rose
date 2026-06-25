package io.zhijun.devservice.core.api.config;

import java.util.ArrayList;
import java.util.List;

import io.zhijun.core.annotation.Incubating;

/**
 * JDBC dev service properties shared by database connector property classes.
 */
@Incubating
public abstract class JdbcDevServiceProperties extends BaseDevServiceProperties {

    public static final String DEFAULT_USERNAME = "rose";
    public static final String DEFAULT_PASSWORD = "rose";
    public static final String DEFAULT_DB_NAME = "rose";

    private String username = DEFAULT_USERNAME;
    private String password = DEFAULT_PASSWORD;
    private String dbName = DEFAULT_DB_NAME;
    private List<String> initScriptPaths = new ArrayList<String>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public List<String> getInitScriptPaths() {
        return initScriptPaths;
    }

    public void setInitScriptPaths(List<String> initScriptPaths) {
        this.initScriptPaths = initScriptPaths;
    }
}
