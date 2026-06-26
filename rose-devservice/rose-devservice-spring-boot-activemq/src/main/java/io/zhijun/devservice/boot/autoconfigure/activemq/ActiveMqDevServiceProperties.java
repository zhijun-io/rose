package io.zhijun.devservice.boot.autoconfigure.activemq;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * ActiveMQ Classic dev service properties.
 */
@ConfigurationProperties(prefix = ActiveMqDevServiceProperties.CONFIG_PREFIX)
public class ActiveMqDevServiceProperties extends BaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.activemq";

    static final String DEFAULT_USERNAME = "rose";
    static final String DEFAULT_PASSWORD = "rose";

    /** Fixed host port for the ActiveMQ web console; 0 selects a random port. */
    private int managementConsolePort = 0;

    /** Broker username injected into connection properties. */
    private String username = DEFAULT_USERNAME;

    /** Broker password injected into connection properties. */
    private String password = DEFAULT_PASSWORD;

    public ActiveMqDevServiceProperties() {
        setImageName("apache/activemq-classic:5.18.3");
        setShared(true);
        setStartupTimeout(Duration.ofSeconds(60));
    }

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
