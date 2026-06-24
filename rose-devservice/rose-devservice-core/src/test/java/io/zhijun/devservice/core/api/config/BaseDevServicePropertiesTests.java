package io.zhijun.devservice.core.api.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link BaseDevServiceProperties} defaults.
 */
class BaseDevServicePropertiesTests {

  @Test
  void defaultMethods() {
    BaseDevServiceProperties properties = new BaseDevServiceProperties() {
      private String imageName = "postgres:16";

      @Override
      public String getImageName() {
        return imageName;
      }

      @Override
      public void setImageName(String imageName) {
        this.imageName = imageName;
      }
    };

    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getEnvironment()).isEmpty();
    assertThat(properties.getNetworkAliases()).isEmpty();
    assertThat(properties.getPort()).isZero();
    assertThat(properties.getResources()).isEmpty();
    assertThat(properties.isShared()).isFalse();
    assertThat(properties.getStartupTimeout()).isEqualTo(Duration.ofSeconds(30));
    assertThat(properties.getVolumes()).isEmpty();

    Map<String, String> environment = new HashMap<String, String>();
    environment.put("KEY", "value");
    List<String> aliases = Arrays.asList("db");
    List<ResourceMapping> resources = Arrays.asList(new ResourceMapping("a", "b"));
    List<VolumeMapping> volumes = Arrays.asList(new VolumeMapping("/host", "/container"));

    properties.setEnabled(false);
    properties.setEnvironment(environment);
    properties.setNetworkAliases(aliases);
    properties.setPort(5432);
    properties.setResources(resources);
    properties.setShared(true);
    properties.setStartupTimeout(Duration.ofMinutes(2));
    properties.setVolumes(volumes);
    properties.setImageName("postgres:17");

    assertThat(properties.getImageName()).isEqualTo("postgres:17");
    assertThat(properties.getEnvironment()).isEqualTo(Collections.<String, String>emptyMap());
    assertThat(properties.getNetworkAliases()).isEqualTo(Collections.<String>emptyList());
  }

}
