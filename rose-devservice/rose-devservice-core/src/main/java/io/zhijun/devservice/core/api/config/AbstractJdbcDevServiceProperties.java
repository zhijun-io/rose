package io.zhijun.devservice.core.api.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base implementation of {@link JdbcDevServiceProperties} that extends
 * {@link AbstractBaseDevServiceProperties} with JDBC-specific fields (username,
 * password, database name, init scripts).
 *
 * @see JdbcDevServiceProperties
 */
public abstract class AbstractJdbcDevServiceProperties extends AbstractBaseDevServiceProperties
        implements JdbcDevServiceProperties {

    private String username = DEFAULT_USERNAME;
    private String password = DEFAULT_PASSWORD;
    private String dbName = DEFAULT_DB_NAME;
    private List<String> initScriptPaths = new ArrayList<String>();

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getDbName() {
        return dbName;
    }

    @Override
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public List<String> getInitScriptPaths() {
        return initScriptPaths;
    }

    @Override
    public void setInitScriptPaths(List<String> initScriptPaths) {
        this.initScriptPaths = initScriptPaths;
    }
}
