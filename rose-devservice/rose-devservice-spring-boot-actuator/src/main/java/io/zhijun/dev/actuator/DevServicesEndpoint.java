package io.zhijun.dev.actuator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.lang.Nullable;

import io.zhijun.dev.api.registration.ContainerInfo;
import io.zhijun.dev.api.registration.DevServiceRegistration;

/**
 * Endpoint for exposing development services information.
 */
@Endpoint(id = "devservices")
public class DevServicesEndpoint {

    private final Map<String, DevServiceRegistration> registrations;

    public DevServicesEndpoint(Map<String, DevServiceRegistration> registrations) {
        this.registrations = registrations;
    }

    @ReadOperation
    public Map<String, ServiceInfoSummary> devServices() {
        Map<String, ServiceInfoSummary> summaries = new java.util.HashMap<String, ServiceInfoSummary>();
        for (DevServiceRegistration registration : registrations.values()) {
            ServiceInfoSummary summary = new ServiceInfoSummary(
                    registration.getName(),
                    registration.getDescription(),
                    ContainerInfoSummary.from(registration.getContainerInfo().get()));
            summaries.put(summary.getName(), summary);
        }
        return summaries;
    }

    @ReadOperation
    public ServiceInfo devService(@Selector String name) {
        DevServiceRegistration registration = registrations.get(name);
        if (registration == null) {
            throw new IllegalArgumentException("Dev service not found: " + name);
        }
        return new ServiceInfo(
                registration.getName(),
                registration.getDescription(),
                registration.getContainerInfo().get());
    }

    public static final class ServiceInfoSummary {

        private final String name;
        @Nullable
        private final String description;
        private final ContainerInfoSummary containerInfo;

        public ServiceInfoSummary(String name, @Nullable String description, ContainerInfoSummary containerInfo) {
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

        public ContainerInfoSummary getContainerInfo() {
            return containerInfo;
        }
    }

    public static final class ContainerInfoSummary {

        private final String id;
        private final String imageName;
        private final List<ContainerInfo.ContainerPort> exposedPorts;

        public ContainerInfoSummary(
                String id,
                String imageName,
                List<ContainerInfo.ContainerPort> exposedPorts) {
            this.id = id;
            this.imageName = imageName;
            this.exposedPorts = new ArrayList<ContainerInfo.ContainerPort>(exposedPorts);
        }

        public static ContainerInfoSummary from(ContainerInfo containerInfo) {
            return new ContainerInfoSummary(
                    containerInfo.getId(),
                    containerInfo.getImageName(),
                    containerInfo.getExposedPorts());
        }

        public String getId() {
            return id;
        }

        public String getImageName() {
            return imageName;
        }

        public List<ContainerInfo.ContainerPort> getExposedPorts() {
            return exposedPorts;
        }
    }

    public static final class ServiceInfo {

        private final String name;
        @Nullable
        private final String description;
        private final ContainerInfo containerInfo;

        public ServiceInfo(String name, @Nullable String description, ContainerInfo containerInfo) {
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

        public ContainerInfo getContainerInfo() {
            return containerInfo;
        }
    }
}
