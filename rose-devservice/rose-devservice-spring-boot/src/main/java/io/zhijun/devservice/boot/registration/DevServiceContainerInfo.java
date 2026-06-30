package io.zhijun.devservice.boot.registration;

import com.github.dockerjava.api.DockerClient;
import io.zhijun.devservice.core.api.registration.ContainerInfo;
import org.testcontainers.DockerClientFactory;

import java.util.*;

/**
 * Utility for extracting container metadata from a running Docker container.
 * <p>
 * Separates Docker client interaction from the container registration domain.
 */
public final class DevServiceContainerInfo {

    private DevServiceContainerInfo() {}

    /**
     * Queries Docker for container metadata by container ID.
     *
     * @param containerId the Docker container ID
     * @return container information including image, names, ports, and labels
     * @throws IllegalStateException if the container cannot be found or query fails
     */
    public static ContainerInfo extractById(String containerId) {
        try {
            DockerClient dockerClient = DockerClientFactory.lazyClient();
            com.github.dockerjava.api.model.Container dockerContainer = dockerClient
                    .listContainersCmd()
                    .withIdFilter(Collections.singleton(containerId))
                    .withShowAll(true)
                    .exec()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Container not found with ID: " + containerId));

            List<String> names = new ArrayList<String>();
            for (String name : dockerContainer.getNames()) {
                names.add(name.charAt(0) == '/' ? name.substring(1) : name);
            }

            List<ContainerInfo.ContainerPort> exposedPorts = new ArrayList<ContainerInfo.ContainerPort>();
            if (dockerContainer.getPorts() != null) {
                for (com.github.dockerjava.api.model.ContainerPort port : dockerContainer.getPorts()) {
                    exposedPorts.add(new ContainerInfo.ContainerPort(
                            port.getIp(), port.getPrivatePort(), port.getPublicPort(), port.getType()));
                }
            }

            Map<String, String> labels =
                    dockerContainer.getLabels() != null ? dockerContainer.getLabels() : new HashMap<String, String>();

            return new ContainerInfo(
                    containerId, dockerContainer.getImage(), names, exposedPorts, labels, dockerContainer.getStatus());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to extract container information for ID: " + containerId, ex);
        }
    }
}
