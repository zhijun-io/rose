package io.zhijun.spring.boot.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PropertyConstantsTest {

    @Test
    void shouldExposeRoseSpringBootPropertyPrefixes() {
        assertThat(PropertyConstants.ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX).isEqualTo("rose.spring.boot.");
        assertThat(PropertyConstants.DEFAULT_CONFIG_ENABLED_PROPERTY_NAME).isEqualTo("rose.default-config.enabled");
        assertThat(PropertyConstants.DEFAULT_CONFIG_LOCATIONS_PROPERTY_NAME).isEqualTo("rose.default-config.locations");
        assertThat(PropertyConstants.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME).isEqualTo("rose.autoconfigure.exclude");
        assertThat(PropertyConstants.ARTIFACTS_COLLISION_ENABLED_PROPERTY_NAME)
                .isEqualTo("rose.diagnostics.artifacts-collision.enabled");
    }
}
