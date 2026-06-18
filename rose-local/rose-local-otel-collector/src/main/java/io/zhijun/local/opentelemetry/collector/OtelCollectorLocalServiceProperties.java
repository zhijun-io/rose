package io.zhijun.local.opentelemetry.collector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.local.api.config.BaseLocalServiceProperties;
import io.zhijun.local.api.config.ResourceMapping;
import io.zhijun.local.api.config.VolumeMapping;

/**
 * OpenTelemetry Collector dev service properties.
 */
@ConfigurationProperties(prefix = OtelCollectorLocalServiceProperties.CONFIG_PREFIX)
public class OtelCollectorLocalServiceProperties implements BaseLocalServiceProperties {

    public static final String CONFIG_PREFIX = "rose.local.otel-collector";

    private boolean enabled = true;
    private String imageName = "otel/opentelemetry-collector-contrib:0.96.0";
    private Map<String, String> environment = new HashMap<String, String>();
    private List<String> networkAliases = new ArrayList<String>();
    private int port = 0;
    private int otlpGrpcPort = 0;
    private List<ResourceMapping> resources = new ArrayList<ResourceMapping>();
    private boolean shared = true;
    private Duration startupTimeout = Duration.ofSeconds(30);
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

    public int getOtlpGrpcPort() {
        return otlpGrpcPort;
    }

    public void setOtlpGrpcPort(int otlpGrpcPort) {
        this.otlpGrpcPort = otlpGrpcPort;
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
