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
        };
        properties.setImageName("postgres:16");

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getEnvironment()).isEmpty();
        assertThat(properties.getNetworkAliases()).isEmpty();
        assertThat(properties.getPort()).isZero();
        assertThat(properties.getResources()).isEmpty();
        assertThat(properties.isShared()).isFalse();
        assertThat(properties.getStartupTimeout()).isEqualTo(BaseDevServiceProperties.DEFAULT_STARTUP_TIMEOUT);
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
        properties.setStartupTimeout(BaseDevServiceProperties.HEAVY_STARTUP_TIMEOUT);
        properties.setVolumes(volumes);
        properties.setImageName("postgres:17");

        assertThat(properties.getImageName()).isEqualTo("postgres:17");
        assertThat(properties.getEnvironment()).isEqualTo(environment);
        assertThat(properties.getNetworkAliases()).isEqualTo(aliases);
    }

    @Test
    void isFixedPortWhenPortIsInvalidThenReturnFalse() {
        assertThat(BaseDevServiceProperties.isFixedPort(-1)).isFalse();
        assertThat(BaseDevServiceProperties.isFixedPort(0)).isFalse();
        assertThat(BaseDevServiceProperties.isFixedPort(65536)).isFalse();
    }

    @Test
    void isFixedPortWhenPortIsValidThenReturnTrue() {
        assertThat(BaseDevServiceProperties.isFixedPort(1234)).isTrue();
        assertThat(BaseDevServiceProperties.isFixedPort(65535)).isTrue();
    }

}
