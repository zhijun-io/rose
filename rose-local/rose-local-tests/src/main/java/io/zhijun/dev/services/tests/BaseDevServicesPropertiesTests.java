package io.zhijun.dev.services.tests;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.api.config.BaseDevServicesProperties;
import io.zhijun.dev.services.api.config.ResourceMapping;
import io.zhijun.dev.services.api.config.VolumeMapping;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base test class for testing {@link BaseDevServicesProperties} implementations.
 *
 * @param <T> the specific {@link BaseDevServicesProperties} implementation type
 */
public abstract class BaseDevServicesPropertiesTests<T extends BaseDevServicesProperties> {

    private static final String TEST_IMAGE_NAME = "test-image:latest";
    private static final int TEST_PORT = 9999;
    private static final Map<String, String> TEST_ENVIRONMENT = mapOf("KEY", "value");
    private static final List<String> TEST_NETWORK_ALIASES = Arrays.asList("network1", "network2");
    private static final Duration TEST_STARTUP_TIMEOUT = Duration.ofMinutes(1);
    private static final ResourceMapping TEST_RESOURCE =
            new ResourceMapping("test-resource.txt", "/tmp/test-resource.txt");
    private static final VolumeMapping TEST_VOLUME =
            new VolumeMapping("/host/path", "/container/path");

    protected abstract T createProperties();

    protected abstract DefaultValues getExpectedDefaults();

    @Test
    void shouldCreateInstanceWithDefaultValues() {
        T properties = createProperties();
        DefaultValues defaults = getExpectedDefaults();

        if (defaults.imageName().isEmpty()) {
            assertThat(properties.getImageName()).isEmpty();
        } else {
            assertThat(properties.getImageName()).contains(defaults.imageName());
        }

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getEnvironment()).isEmpty();
        assertThat(properties.getNetworkAliases()).isEmpty();
        assertThat(properties.getPort()).isEqualTo(0);
        assertThat(properties.getResources()).isEmpty();
        assertThat(properties.isShared()).isEqualTo(defaults.shared());
        assertThat(properties.getStartupTimeout()).isEqualTo(defaults.startupTimeout());
        assertThat(properties.getVolumes()).isEmpty();
    }

    @Test
    void shouldUpdateCommonProperties() {
        T properties = createProperties();
        DefaultValues defaults = getExpectedDefaults();

        properties.setEnabled(false);
        assertThat(properties.isEnabled()).isFalse();

        properties.setEnvironment(TEST_ENVIRONMENT);
        assertThat(properties.getEnvironment()).containsEntry("KEY", "value");

        properties.setImageName(TEST_IMAGE_NAME);
        assertThat(properties.getImageName()).isEqualTo(TEST_IMAGE_NAME);

        properties.setNetworkAliases(TEST_NETWORK_ALIASES);
        assertThat(properties.getNetworkAliases()).containsExactly("network1", "network2");

        properties.setPort(TEST_PORT);
        assertThat(properties.getPort()).isEqualTo(TEST_PORT);

        properties.setResources(Collections.singletonList(TEST_RESOURCE));
        assertThat(properties.getResources()).hasSize(1);
        assertThat(properties.getResources().get(0).getSourcePath()).isEqualTo("test-resource.txt");
        assertThat(properties.getResources().get(0).getContainerPath()).isEqualTo("/tmp/test-resource.txt");

        properties.setShared(!defaults.shared());
        assertThat(properties.isShared()).isEqualTo(!defaults.shared());

        properties.setStartupTimeout(TEST_STARTUP_TIMEOUT);
        assertThat(properties.getStartupTimeout()).isEqualTo(TEST_STARTUP_TIMEOUT);

        properties.setVolumes(Collections.singletonList(TEST_VOLUME));
        assertThat(properties.getVolumes()).hasSize(1);
        assertThat(properties.getVolumes().get(0).getHostPath()).isEqualTo("/host/path");
        assertThat(properties.getVolumes().get(0).getContainerPath()).isEqualTo("/container/path");
    }

    public static final class DefaultValues {

        private final String imageName;
        private final boolean shared;
        private final Duration startupTimeout;

        public DefaultValues(String imageName, boolean shared, Duration startupTimeout) {
            this.imageName = imageName;
            this.shared = shared;
            this.startupTimeout = startupTimeout;
        }

        public String imageName() {
            return imageName;
        }

        public boolean shared() {
            return shared;
        }

        public Duration startupTimeout() {
            return startupTimeout;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {

            private String imageName = "";
            private boolean shared = false;
            private Duration startupTimeout = Duration.ofSeconds(30);

            public Builder imageName(String imageName) {
                this.imageName = imageName;
                return this;
            }

            public Builder shared(boolean shared) {
                this.shared = shared;
                return this;
            }

            public Builder startupTimeout(Duration startupTimeout) {
                this.startupTimeout = startupTimeout;
                return this;
            }

            public DefaultValues build() {
                return new DefaultValues(imageName, shared, startupTimeout);
            }
        }
    }

    private static Map<String, String> mapOf(String key, String value) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(key, value);
        return map;
    }
}
