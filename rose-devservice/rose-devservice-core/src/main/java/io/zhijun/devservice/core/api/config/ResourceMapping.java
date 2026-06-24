package io.zhijun.devservice.core.api.config;

/**
 * Classpath or filesystem resource copied into a container.
 */
public final class ResourceMapping {

    private String sourcePath;
    private String containerPath;

    public ResourceMapping() {
    }

    public ResourceMapping(String sourcePath, String containerPath) {
        this.sourcePath = sourcePath;
        this.containerPath = containerPath;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }
}
