package io.zhijun.devservice.core.api.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Range;

import org.apiguardian.api.API;

/**
 * Base dev service properties shared by all connector property classes.
 */
@API(status = API.Status.EXPERIMENTAL)
public abstract class BaseDevServiceProperties {

    public static final int RANDOM_PORT = 0;

    public static final int MAX_PORT = 65535;

    private static final Range<Integer> VALID_PORT_RANGE = Range.between(1, MAX_PORT);

    public static final Duration DEFAULT_STARTUP_TIMEOUT = Duration.ofSeconds(30);

    public static final Duration SLOW_STARTUP_TIMEOUT = Duration.ofSeconds(60);

    public static final Duration HEAVY_STARTUP_TIMEOUT = Duration.ofMinutes(2);

    /**
     * Whether to start the Dev Service container.
     */
    private boolean enabled = true;

    /**
     * Docker image for the Dev Service.
     */
    private String imageName;

    private Map<String, String> environment = new HashMap<String, String>();
    private List<String> networkAliases = new ArrayList<String>();

    /**
     * Fixed host port; 0 selects a random port.
     */
    private int port = RANDOM_PORT;

    private List<ResourceMapping> resources = new ArrayList<ResourceMapping>();

    /**
     * Reuse a shared container across application restarts.
     */
    private boolean shared = false;

    /**
     * Maximum time to wait for the container to become ready.
     */
    private Duration startupTimeout = DEFAULT_STARTUP_TIMEOUT;

    private List<VolumeMapping> volumes = new ArrayList<VolumeMapping>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public List<String> getNetworkAliases() {
        return networkAliases;
    }

    public void setNetworkAliases(List<String> networkAliases) {
        this.networkAliases = networkAliases;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<ResourceMapping> getResources() {
        return resources;
    }

    public void setResources(List<ResourceMapping> resources) {
        this.resources = resources;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public Duration getStartupTimeout() {
        return startupTimeout;
    }

    public void setStartupTimeout(Duration startupTimeout) {
        this.startupTimeout = startupTimeout;
    }

    public List<VolumeMapping> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<VolumeMapping> volumes) {
        this.volumes = volumes;
    }

    /**
     * Whether {@code port} is a fixed host port (not {@link #RANDOM_PORT}).
     */
    public static boolean isFixedPort(int port) {
        return VALID_PORT_RANGE.contains(port);
    }
}
