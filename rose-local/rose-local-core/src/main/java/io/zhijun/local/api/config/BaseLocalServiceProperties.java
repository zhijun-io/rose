package io.zhijun.local.api.config;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.zhijun.core.annotation.Incubating;

/**
 * Base dev service properties.
 */
@Incubating
public interface BaseLocalServiceProperties {

    default boolean isEnabled() {
        return true;
    }

    String getImageName();

    default Map<String, String> getEnvironment() {
        return Collections.emptyMap();
    }

    default List<String> getNetworkAliases() {
        return Collections.emptyList();
    }

    default int getPort() {
        return 0;
    }

    default List<ResourceMapping> getResources() {
        return Collections.emptyList();
    }

    default boolean isShared() {
        return false;
    }

    default Duration getStartupTimeout() {
        return Duration.ofSeconds(30);
    }

    default List<VolumeMapping> getVolumes() {
        return Collections.emptyList();
    }

    default void setEnabled(boolean enabled) {
    }

    void setImageName(String imageName);

    default void setEnvironment(Map<String, String> environment) {
    }

    default void setNetworkAliases(List<String> networkAliases) {
    }

    default void setPort(int port) {
    }

    default void setResources(List<ResourceMapping> resources) {
    }

    default void setShared(boolean shared) {
    }

    default void setStartupTimeout(Duration startupTimeout) {
    }

    default void setVolumes(List<VolumeMapping> volumes) {
    }
}
