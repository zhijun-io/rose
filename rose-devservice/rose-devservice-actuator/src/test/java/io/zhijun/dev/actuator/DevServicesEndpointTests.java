package io.zhijun.dev.actuator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.zhijun.dev.actuator.DevServicesEndpoint.ServiceInfo;
import io.zhijun.dev.actuator.DevServicesEndpoint.ServiceInfoSummary;
import io.zhijun.dev.api.registration.ContainerInfo;
import io.zhijun.dev.api.registration.LocalServiceRegistration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevServicesEndpointTests {

    private Map<String, LocalServiceRegistration> registrations;
    private DevServicesEndpoint endpoint;

    @BeforeEach
    void setUp() {
        registrations = new HashMap<String, LocalServiceRegistration>();
        endpoint = new DevServicesEndpoint(registrations);
    }

    @Test
    void devServicesReturnsEmptyMapWhenNoRegistrations() {
        assertThat(endpoint.devServices()).isEmpty();
    }

    @Test
    void devServicesReturnsSingleServiceWhenOneRegistration() {
        ContainerInfo containerInfo = createContainerInfo("container-1", "postgres:18", "running");
        registrations.put("postgres", new LocalServiceRegistration("postgres", "PostgreSQL Database", () -> containerInfo));

        Map<String, ServiceInfoSummary> result = endpoint.devServices();

        assertThat(result).hasSize(1).containsKey("postgres");
        ServiceInfoSummary summary = result.get("postgres");
        assertThat(summary.getName()).isEqualTo("postgres");
        assertThat(summary.getDescription()).isEqualTo("PostgreSQL Database");
        assertThat(summary.getContainerInfo().getId()).isEqualTo("container-1");
        assertThat(summary.getContainerInfo().getImageName()).isEqualTo("postgres:18");
        assertThat(summary.getContainerInfo().getExposedPorts()).hasSize(1);
    }

    @Test
    void devServiceReturnsServiceInfoWhenServiceExists() {
        ContainerInfo containerInfo = createContainerInfo("container-1", "postgres:18", "running");
        registrations.put("postgres", new LocalServiceRegistration("postgres", "PostgreSQL Database", () -> containerInfo));

        ServiceInfo result = endpoint.devService("postgres");

        assertThat(result.getName()).isEqualTo("postgres");
        assertThat(result.getDescription()).isEqualTo("PostgreSQL Database");
        assertThat(result.getContainerInfo().getId()).isEqualTo("container-1");
        assertThat(result.getContainerInfo().getStatus()).isEqualTo("running");
    }

    @Test
    void devServiceThrowsExceptionWhenServiceNotFound() {
        assertThatThrownBy(() -> endpoint.devService("postgres"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dev service not found: postgres");
    }

    private ContainerInfo createContainerInfo(String id, String imageName, String status) {
        List<ContainerInfo.ContainerPort> ports = new ArrayList<ContainerInfo.ContainerPort>();
        ports.add(new ContainerInfo.ContainerPort("0.0.0.0", 5432, 5432, "tcp"));
        Map<String, String> labels = new HashMap<String, String>();
        labels.put("app", "test");
        List<String> names = new ArrayList<String>();
        names.add("postgres-container");
        return new ContainerInfo(id, imageName, names, ports, labels, status);
    }
}
