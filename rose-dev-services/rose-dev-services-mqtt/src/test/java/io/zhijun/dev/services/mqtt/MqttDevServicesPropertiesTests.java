package io.zhijun.dev.services.mqtt;

import java.time.Duration;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.api.config.ResourceMapping;
import io.zhijun.dev.services.api.config.VolumeMapping;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MqttDevServicesProperties}.
 */
class MqttDevServicesPropertiesTests {

  @Test
  void defaultsAndMutators() {
    MqttDevServicesProperties properties = new MqttDevServicesProperties();

    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getImageName()).isEqualTo("hivemq/hivemq-ce:2024.1");
    assertThat(properties.getEnvironment()).isEmpty();
    assertThat(properties.getNetworkAliases()).isEmpty();
    assertThat(properties.getPort()).isZero();
    assertThat(properties.getResources()).isEmpty();
    assertThat(properties.isShared()).isFalse();
    assertThat(properties.getStartupTimeout()).isEqualTo(Duration.ofSeconds(60));
    assertThat(properties.getVolumes()).isEmpty();

    properties.setEnabled(false);
    properties.setImageName("hivemq/hivemq-ce:latest");
    properties.setEnvironment(Collections.singletonMap("DEBUG", "true"));
    properties.setNetworkAliases(Collections.singletonList("mqtt"));
    properties.setPort(1883);
    properties.setResources(Collections.singletonList(new ResourceMapping("cfg", "/cfg")));
    properties.setShared(true);
    properties.setStartupTimeout(Duration.ofMinutes(1));
    properties.setVolumes(Collections.singletonList(new VolumeMapping("/host", "/container")));

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.getImageName()).isEqualTo("hivemq/hivemq-ce:latest");
    assertThat(properties.getEnvironment()).containsEntry("DEBUG", "true");
    assertThat(properties.getNetworkAliases()).containsExactly("mqtt");
    assertThat(properties.getPort()).isEqualTo(1883);
    assertThat(properties.getResources()).hasSize(1);
    assertThat(properties.isShared()).isTrue();
    assertThat(properties.getStartupTimeout()).isEqualTo(Duration.ofMinutes(1));
    assertThat(properties.getVolumes()).hasSize(1);
  }

}
