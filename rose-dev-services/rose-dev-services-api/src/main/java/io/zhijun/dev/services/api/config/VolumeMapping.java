package io.zhijun.dev.services.api.config;

/**
 * Host path bind-mounted into a container.
 */
public final class VolumeMapping {

    private String hostPath;
    private String containerPath;

    public VolumeMapping() {
    }

    public VolumeMapping(String hostPath, String containerPath) {
        this.hostPath = hostPath;
        this.containerPath = containerPath;
    }

    public String getHostPath() {
        return hostPath;
    }

    public void setHostPath(String hostPath) {
        this.hostPath = hostPath;
    }

    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }
}
