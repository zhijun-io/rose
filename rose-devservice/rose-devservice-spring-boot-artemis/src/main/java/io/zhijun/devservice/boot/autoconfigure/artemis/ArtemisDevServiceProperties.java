package io.zhijun.devservice.boot.autoconfigure.artemis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.AbstractBaseDevServiceProperties;

/**
 * ActiveMQ Artemis dev service properties.
 */
@ConfigurationProperties(prefix = ArtemisDevServiceProperties.CONFIG_PREFIX)
public class ArtemisDevServiceProperties extends AbstractBaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.artemis";

    static final String DEFAULT_USERNAME = "rose";
    static final String DEFAULT_PASSWORD = "rose";

    private int managementConsolePort = 0;
    private String username = DEFAULT_USERNAME;
    private String password = DEFAULT_PASSWORD;

    public ArtemisDevServiceProperties() {
        setImageName("apache/activemq-artemis:2.31.2-alpine");
        setShared(true);
        setStartupTimeout(java.time.Duration.ofSeconds(60));
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
