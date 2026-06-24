package io.zhijun.devservice.autoconfigure.postgresql;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.api.config.JdbcDevServiceProperties;
import io.zhijun.devservice.api.config.ResourceMapping;
import io.zhijun.devservice.api.config.VolumeMapping;

/**
 * PostgreSQL dev service properties.
 */
@ConfigurationProperties(prefix = PostgresqlDevServiceProperties.CONFIG_PREFIX)
public class PostgresqlDevServiceProperties implements JdbcDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.postgresql";

    private boolean enabled = true;
    private String imageName = "postgres:18-alpine";
    private Map<String, String> environment = new HashMap<String, String>();
    private List<String> networkAliases = new ArrayList<String>();
    private int port = 0;
    private List<ResourceMapping> resources = new ArrayList<ResourceMapping>();
    private boolean shared = false;
    private Duration startupTimeout = Duration.ofSeconds(30);
    private List<VolumeMapping> volumes = new ArrayList<VolumeMapping>();
    private String username = DEFAULT_USERNAME;
    private String password = DEFAULT_PASSWORD;
    private String dbName = DEFAULT_DB_NAME;
    private List<String> initScriptPaths = new ArrayList<String>();

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getImageName() {
        return imageName;
    }

    @Override
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public Map<String, String> getEnvironment() {
        return environment;
    }

    @Override
    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    @Override
    public List<String> getNetworkAliases() {
        return networkAliases;
    }

    @Override
    public void setNetworkAliases(List<String> networkAliases) {
        this.networkAliases = networkAliases;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public List<ResourceMapping> getResources() {
        return resources;
    }

    @Override
    public void setResources(List<ResourceMapping> resources) {
        this.resources = resources;
    }

    @Override
    public boolean isShared() {
        return shared;
    }

    @Override
    public void setShared(boolean shared) {
        this.shared = shared;
    }

    @Override
    public Duration getStartupTimeout() {
        return startupTimeout;
    }

    @Override
    public void setStartupTimeout(Duration startupTimeout) {
        this.startupTimeout = startupTimeout;
    }

    @Override
    public List<VolumeMapping> getVolumes() {
        return volumes;
    }

    @Override
    public void setVolumes(List<VolumeMapping> volumes) {
        this.volumes = volumes;
    }

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
