package io.zhijun.devservice.core.api.registration;

import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

import org.apiguardian.api.API;

/**
 * Describes a registered dev service.
 */
@API(status = API.Status.EXPERIMENTAL)
public final class DevServiceRegistration {

    private final String name;
    private final String description;
    private final Supplier<ContainerInfo> containerInfo;

    public DevServiceRegistration(String name, String description, Supplier<ContainerInfo> containerInfo) {
        Validate.notBlank(name, "name cannot be null or empty");
        Validate.notNull(containerInfo, "containerInfo cannot be null");
        this.name = name;
        this.description = description;
        this.containerInfo = containerInfo;
    }

    public String getName() {
        return name;
    }

    /**
     * @return description, or {@code null} when not provided
     */
    public String getDescription() {
        return description;
    }

    public Supplier<ContainerInfo> getContainerInfo() {
        return containerInfo;
    }
}
