package io.zhijun.devservice.core.api;

import java.util.Collections;
import java.util.Arrays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.core.api.registration.ContainerInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for {@link ContainerInfo}.
 */
class ContainerInfoTests {

    private static Map<String, String> mapOf(String k1, String v1) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(k1, v1);
        return map;
    }

    private static Map<String, String> mapOf(String k1, String v1, String k2, String v2) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    @Test
    void whenIdIsNullThenThrow() {
        assertThatThrownBy(() -> ContainerInfo.builder().id(null).imageName("image").status("running").build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id cannot be null or empty");
    }

    @Test
    void whenIdIsEmptyThenThrow() {
        assertThatThrownBy(() -> ContainerInfo.builder().id("").imageName("image").status("running").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id cannot be null or empty");
    }

    @Test
    void whenImageNameIsNullThenThrow() {
        assertThatThrownBy(() -> ContainerInfo.builder().id("id123").imageName(null).status("running").build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("imageName cannot be null or empty");
    }

    @Test
    void whenImageNameIsEmptyThenThrow() {
        assertThatThrownBy(() -> ContainerInfo.builder().id("id123").imageName("").status("running").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("imageName cannot be null or empty");
    }

    @Test
    void whenNamesIsNullThenThrow() {
        assertThatThrownBy(() -> ContainerInfo.builder().id("id123").imageName("image").names(null).status("running").build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("names cannot be null");
    }

    @Test
    void whenExposedPortsIsNullThenThrow() {
        assertThatThrownBy(() -> ContainerInfo.builder().id("id123").imageName("image").exposedPorts(null).status("running").build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("exposedPorts cannot be null");
    }

    @Test
    void whenLabelsIsNullThenThrow() {
        assertThatThrownBy(() -> ContainerInfo.builder().id("id123").imageName("image").labels(null).status("running").build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("labels cannot be null");
    }

    @Test
    void whenStatusIsNullThenThrow() {
        assertThatThrownBy(() -> ContainerInfo.builder().id("id123").imageName("image").status(null).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("status cannot be null or empty");
    }

    @Test
    void whenStatusIsEmptyThenThrow() {
        assertThatThrownBy(() -> ContainerInfo.builder().id("id123").imageName("image").status("").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("status cannot be null or empty");
    }

    @Test
    void whenAllFieldsAreValidThenCreate() {
        List<String> names = Arrays.asList("container1", "container2");
        ContainerInfo.ContainerPort port = new ContainerInfo.ContainerPort("127.0.0.1", 8080, 8080, "tcp");
        List<ContainerInfo.ContainerPort> exposedPorts = Collections.singletonList(port);
        Map<String, String> labels = mapOf("key1", "value1", "key2", "value2");

        ContainerInfo containerInfo = ContainerInfo.builder()
                .id("id123")
                .imageName("image")
                .names(names)
                .exposedPorts(exposedPorts)
                .labels(labels)
                .status("running")
                .build();

        assertThat(containerInfo.getId()).isEqualTo("id123");
        assertThat(containerInfo.getImageName()).isEqualTo("image");
        assertThat(containerInfo.getNames()).containsExactly("container1", "container2");
        assertThat(containerInfo.getExposedPorts()).containsExactly(port);
        assertThat(containerInfo.getLabels()).containsEntry("key1", "value1").containsEntry("key2", "value2");
        assertThat(containerInfo.getStatus()).isEqualTo("running");
    }

    @Test
    void whenCreatedThenCollectionsAreImmutable() {
        ArrayList<String> names = new ArrayList<String>(Collections.singletonList("container1"));
        ContainerInfo.ContainerPort port = new ContainerInfo.ContainerPort("127.0.0.1", 8080, 8080, "tcp");
        ArrayList<ContainerInfo.ContainerPort> exposedPorts = new ArrayList<ContainerInfo.ContainerPort>(Collections.singletonList(port));
        HashMap<String, String> labels = new HashMap<String, String>(mapOf("key1", "value1"));

        ContainerInfo containerInfo = ContainerInfo.builder()
                .id("id123")
                .imageName("image")
                .names(names)
                .exposedPorts(exposedPorts)
                .labels(labels)
                .status("running")
                .build();

        // Modify the original collections
        names.add("container2");
        exposedPorts.add(new ContainerInfo.ContainerPort("127.0.0.1", 9090, 9090, "tcp"));
        labels.put("key2", "value2");

        // Verify that the ContainerInfo collections are unchanged (defensive copies were made)
        assertThat(containerInfo.getNames()).hasSize(1).containsExactly("container1");
        assertThat(containerInfo.getExposedPorts()).hasSize(1).containsExactly(port);
        assertThat(containerInfo.getLabels()).hasSize(1).containsEntry("key1", "value1");

        // Verify that the returned collections are immutable
        assertThatThrownBy(() -> containerInfo.getNames().add("container3"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> containerInfo.getExposedPorts().add(new ContainerInfo.ContainerPort("127.0.0.1", 9090, 9090, "tcp")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> containerInfo.getLabels().put("key2", "value2"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void whenContainerPortCreatedWithNullFieldsThenCreate() {
        ContainerInfo.ContainerPort containerPort = new ContainerInfo.ContainerPort(null, null, null, null);

        assertThat(containerPort.getIp()).isNull();
        assertThat(containerPort.getPrivatePort()).isNull();
        assertThat(containerPort.getPublicPort()).isNull();
        assertThat(containerPort.getType()).isNull();
    }

}
