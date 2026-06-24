package io.zhijun.devservice.container;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.MountableFile;

import io.zhijun.devservice.bootstrap.BootstrapMode;
import io.zhijun.devservice.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.api.config.JdbcDevServiceProperties;
import io.zhijun.devservice.api.config.ResourceMapping;
import io.zhijun.devservice.api.config.VolumeMapping;

/**
 * Applies dev service properties to Testcontainers instances.
 */
public final class ContainerConfigurer {

    private static final String RESOURCE_PREFIX_CLASSPATH = "classpath:";
    private static final String RESOURCE_PREFIX_FILE = "file:";

    /**
     * Configures base container settings for dev services.
     */
    public static void base(GenericContainer<?> container, BaseDevServiceProperties properties) {
        container
                .withEnv(properties.getEnvironment())
                .withNetworkAliases(properties.getNetworkAliases().toArray(new String[0]))
                .withStartupTimeout(properties.getStartupTimeout())
                .withReuse(isDevMode() && properties.isShared());

        resources(container, properties);
        volumes(container, properties);
    }

    public static void resources(GenericContainer<?> container, BaseDevServiceProperties properties) {
        for (ResourceMapping resource : properties.getResources()) {
            Assert.hasText(resource.getSourcePath(), "the source path in a resource mapping cannot be null or empty.");
            Assert.hasText(resource.getContainerPath(), "the container path in a resource mapping cannot be null or empty.");

            MountableFile mountableFile = resolveMountableFile(resource.getSourcePath());
            container.withCopyFileToContainer(mountableFile, resource.getContainerPath());
        }
    }

    private static MountableFile resolveMountableFile(String resourcePath) {
        if (resourcePath.startsWith(RESOURCE_PREFIX_CLASSPATH)) {
            String path = resourcePath.substring(RESOURCE_PREFIX_CLASSPATH.length());
            return MountableFile.forClasspathResource(path);
        }

        if (resourcePath.startsWith(RESOURCE_PREFIX_FILE)) {
            String path = resourcePath.substring(RESOURCE_PREFIX_FILE.length());
            return MountableFile.forHostPath(path);
        }

        ClassPathResource classpathResource = new ClassPathResource(resourcePath);
        if (classpathResource.exists()) {
            return MountableFile.forClasspathResource(resourcePath);
        }

        FileSystemResource fileResource = new FileSystemResource(resourcePath);
        if (fileResource.exists()) {
            return MountableFile.forHostPath(resourcePath);
        }

        throw new IllegalArgumentException("Resource not found in classpath or filesystem: " + resourcePath);
    }

    public static void volumes(GenericContainer<?> container, BaseDevServiceProperties properties) {
        for (VolumeMapping mapping : properties.getVolumes()) {
            container.withFileSystemBind(mapping.getHostPath(), mapping.getContainerPath(), BindMode.READ_WRITE);
        }
    }

    public static void jdbc(JdbcDatabaseContainer<?> container, JdbcDevServiceProperties properties) {
        container
                .withUsername(properties.getUsername())
                .withPassword(properties.getPassword())
                .withDatabaseName(properties.getDbName())
                .withInitScripts(properties.getInitScriptPaths());
    }

    private static boolean isDevMode() {
        return BootstrapMode.DEV.equals(BootstrapMode.detect());
    }

    private ContainerConfigurer() {
    }
}
