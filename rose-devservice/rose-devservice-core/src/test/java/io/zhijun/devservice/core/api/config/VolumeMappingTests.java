package io.zhijun.devservice.core.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link VolumeMapping}.
 */
class VolumeMappingTests {

    @Test
    void accessors() {
        VolumeMapping mapping = new VolumeMapping();
        mapping.setHostPath("/host");
        mapping.setContainerPath("/container");

        assertThat(mapping.getHostPath()).isEqualTo("/host");
        assertThat(mapping.getContainerPath()).isEqualTo("/container");
    }

    @Test
    void constructor() {
        VolumeMapping mapping = new VolumeMapping("/data", "/var/lib/data");

        assertThat(mapping.getHostPath()).isEqualTo("/data");
        assertThat(mapping.getContainerPath()).isEqualTo("/var/lib/data");
    }
}
