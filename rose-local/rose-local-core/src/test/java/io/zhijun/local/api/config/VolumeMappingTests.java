package io.zhijun.local.api.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link VolumeMapping}.
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
