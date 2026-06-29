package io.zhijun.devservice.core.api.config;

import org.apiguardian.api.API;

/**
 * Dev service properties shared by message broker connector property classes.
 */
@API(status = API.Status.EXPERIMENTAL)
public abstract class MessagingDevServiceProperties extends BaseDevServiceProperties {

    /** Fixed host port for the broker web console; 0 selects a random port. */
    private int managementConsolePort = RANDOM_PORT;

    /** Broker username injected into connection properties. */
    private String username = DevServiceCredentials.DEFAULT_USERNAME;

    /** Broker password injected into connection properties. */
    private String password = DevServiceCredentials.DEFAULT_PASSWORD;

    public int getManagementConsolePort() {
        return managementConsolePort;
    }

    public void setManagementConsolePort(int managementConsolePort) {
        this.managementConsolePort = managementConsolePort;
    }

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
}
