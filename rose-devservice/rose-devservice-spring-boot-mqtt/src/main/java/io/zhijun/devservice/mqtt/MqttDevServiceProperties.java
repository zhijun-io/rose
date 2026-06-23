package io.zhijun.devservice.mqtt;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.api.config.ResourceMapping;
import io.zhijun.devservice.api.config.VolumeMapping;

/**
 * MQTT dev service properties.
 */
@ConfigurationProperties(prefix = MqttDevServiceProperties.CONFIG_PREFIX)
public class MqttDevServiceProperties implements BaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.mqtt";

    private boolean enabled = true;
    private String imageName = "hivemq/hivemq-ce:2024.1";
    private Map<String, String> environment = new HashMap<String, String>();
    private List<String> networkAliases = new ArrayList<String>();
    private int port = 0;
    private List<ResourceMapping> resources = new ArrayList<ResourceMapping>();
    private boolean shared = false;
    private Duration startupTimeout = Duration.ofSeconds(60);
    private List<VolumeMapping> volumes = new ArrayList<VolumeMapping>();

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
}
