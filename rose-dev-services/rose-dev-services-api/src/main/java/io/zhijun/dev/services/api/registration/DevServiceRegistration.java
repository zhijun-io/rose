package io.zhijun.dev.services.api.registration;

import java.util.function.Supplier;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import io.zhijun.core.annotation.Incubating;

/**
 * Describes a registered dev service.
 */
@Incubating
public final class DevServiceRegistration {

    private final String name;
    @Nullable
    private final String description;
    private final Supplier<ContainerInfo> containerInfo;

    public DevServiceRegistration(
            String name,
            @Nullable String description,
            Supplier<ContainerInfo> containerInfo) {
        Assert.hasText(name, "name cannot be null or empty");
        Assert.notNull(containerInfo, "containerInfo cannot be null");
        this.name = name;
        this.description = description;
        this.containerInfo = containerInfo;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public Supplier<ContainerInfo> getContainerInfo() {
        return containerInfo;
    }
}
