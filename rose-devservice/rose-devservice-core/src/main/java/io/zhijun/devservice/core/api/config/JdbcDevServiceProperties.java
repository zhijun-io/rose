package io.zhijun.devservice.core.api.config;

import java.util.ArrayList;
import java.util.List;


/**
 * JDBC dev service properties shared by database connector property classes.
 */
public abstract class JdbcDevServiceProperties extends BaseDevServiceProperties {

    /**
     * Database username injected into the JDBC URL.
     */
    private String username = DevServiceCredentials.DEFAULT_USERNAME;

    /**
     * Database password injected into the JDBC URL.
     */
    private String password = DevServiceCredentials.DEFAULT_PASSWORD;

    /**
     * Database name created in the Dev Service container.
     */
    private String dbName = DevServiceCredentials.DEFAULT_DB_NAME;

    /**
     * Classpath or file paths of SQL scripts run at container startup.
     */
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
